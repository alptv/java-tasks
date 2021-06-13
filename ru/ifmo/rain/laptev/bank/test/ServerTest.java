package ru.ifmo.rain.laptev.bank.test;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static ru.ifmo.rain.laptev.bank.test.BankTestUtils.NAMES;
import static ru.ifmo.rain.laptev.bank.test.BankTestUtils.SURNAMES;
import static ru.ifmo.rain.laptev.bank.test.BankTestUtils.PASSPORTS;
import static ru.ifmo.rain.laptev.bank.test.BankTestUtils.ACCOUNTS;
import static ru.ifmo.rain.laptev.bank.test.BankTestUtils.AMOUNTS;
import static ru.ifmo.rain.laptev.bank.test.BankTestUtils.DATA_ARRAYS_LENGTH;
import static ru.ifmo.rain.laptev.bank.test.BankTestUtils.NULL_STRING;
import static ru.ifmo.rain.laptev.bank.test.BankTestUtils.newBank;

import ru.ifmo.rain.laptev.bank.server.ServerException;
import ru.ifmo.rain.laptev.bank.common.Account;
import ru.ifmo.rain.laptev.bank.common.Bank;
import ru.ifmo.rain.laptev.bank.common.Person;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

class ServerTest {
    private static Registry registry;

    @BeforeAll
    static void startRmi() {
        registry = BankTestUtils.startRMI();
    }


    private static abstract class PersonTest {

        private Person getPerson(final Bank bank, final int passport) throws RemoteException {
            if (isLocalPerson()) {
                return bank.getLocalPerson(passport);
            } else {
                return bank.getRemotePerson(passport);
            }
        }

        private Person registerPerson(final Bank bank, final String name, final String surname, final int passport) throws RemoteException {
            if (isLocalPerson()) {
                return bank.registerLocalPerson(name, surname, passport);
            } else {
                return bank.registerRemotePerson(name, surname, passport);
            }
        }

        abstract boolean isLocalPerson();

        @Test
        void test1_unregisteredPersons() throws RemoteException, ServerException {
            final Bank bank = newBank(registry);
            for (final int passport : PASSPORTS) {
                assertNull(getPerson(bank, passport));
            }
        }

        @Test
        void test2_personRegistrationAndGetter() throws RemoteException, ServerException {
            final Bank bank = newBank(registry);
            for (int i = 0; i < DATA_ARRAYS_LENGTH; i++) {
                final Person registeredPerson = registerPerson(bank, NAMES[i], SURNAMES[i], PASSPORTS[i]);
                final Person takenPerson = getPerson(bank, PASSPORTS[i]);
                assertEquals(registeredPerson.getName(), takenPerson.getName());
                assertEquals(registeredPerson.getSurname(), takenPerson.getSurname());
                assertEquals(registeredPerson.getPassport(), takenPerson.getPassport());
            }
        }

        @Test
        void test3_samePassportPersonRegistration() throws RemoteException, ServerException {
            final Bank bank = newBank(registry);
            for (final int passport : PASSPORTS) {
                registerPerson(bank, NULL_STRING, NULL_STRING, passport);
                for (final String name : NAMES) {
                    for (final String surname : SURNAMES) {
                        assertNull(registerPerson(bank, name, surname, passport));
                    }
                }
            }
        }

        @Test
        void test4_unregisteredAccountCreation() throws RemoteException, ServerException {
            final Bank bank = newBank(registry);
            final Person person = registerPerson(bank, NULL_STRING, NULL_STRING, 0);
            for (final String account : ACCOUNTS) {
                assertNull(person.getAccount(account));
            }
        }

        @Test
        void test5_accountCreationAndGetter() throws RemoteException, ServerException {
            final Bank bank = newBank(registry);
            final Person person = registerPerson(bank, NULL_STRING, NULL_STRING, 0);
            for (int i = 0; i < DATA_ARRAYS_LENGTH; i++) {
                Account createdAccount = person.createAccount(ACCOUNTS[i]);
                createdAccount.addAmount(AMOUNTS[i]);
                Account takenAccount = person.getAccount(ACCOUNTS[i]);
                assertEquals(createdAccount.getId(), takenAccount.getId());
                assertEquals(createdAccount.getAmount(), AMOUNTS[i]);
                assertEquals(takenAccount.getAmount(), AMOUNTS[i]);
            }
        }

        @Test
        void test6_accountAddingMoney() throws RemoteException, InterruptedException, ServerException {
            final int THREAD_COUNT = 15;
            final int PER_THREAD_MONEY = 100;

            final Bank bank = newBank(registry);
            final Account account = registerPerson(bank, NULL_STRING, NULL_STRING, 0).createAccount(NULL_STRING);
            final Thread[] threads = new Thread[THREAD_COUNT];
            for (int i = 0; i < THREAD_COUNT; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < PER_THREAD_MONEY; j++) {
                        try {
                            account.addAmount(1);
                        } catch (RemoteException ignored) {
                        }
                    }
                });
                threads[i].start();
            }
            for (final Thread thread : threads) {
                thread.join();
            }
            assertEquals(THREAD_COUNT * PER_THREAD_MONEY, account.getAmount());
        }

        @Test
        void test7_accountChanges() throws RemoteException, ServerException {
            final Bank bank = newBank(registry);
            final Person person = registerPerson(bank, NULL_STRING, NULL_STRING, 0);
            for (int i = 0; i < DATA_ARRAYS_LENGTH; i++) {
                person.createAccount(ACCOUNTS[i]).addAmount(AMOUNTS[i]);
            }
            final Person remotePerson = bank.getRemotePerson(0);
            for (int i = 0; i < DATA_ARRAYS_LENGTH; i++) {
                final Account account = remotePerson.getAccount(ACCOUNTS[i]);
                if (isLocalPerson()) {
                    assertNull(account);
                } else {
                    assertNotNull(account);
                    assertEquals(account.getAmount(), AMOUNTS[i]);
                }
            }
        }
    }

    @Nested
    class LocalPersonTest extends PersonTest {
        @Override
        boolean isLocalPerson() {
            return true;
        }
    }

    @Nested
    class RemotePersonTest extends PersonTest {
        @Override
        boolean isLocalPerson() {
            return false;
        }
    }

}
