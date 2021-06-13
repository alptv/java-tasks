package ru.ifmo.rain.laptev.bank.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.ifmo.rain.laptev.bank.client.Client;

import static org.junit.jupiter.api.Assertions.*;
import static ru.ifmo.rain.laptev.bank.test.BankTestUtils.*;
import ru.ifmo.rain.laptev.bank.client.ClientException;
import ru.ifmo.rain.laptev.bank.server.ServerException;
import ru.ifmo.rain.laptev.bank.common.Bank;
import ru.ifmo.rain.laptev.bank.common.Person;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Arrays;

class ClientTest {
    private static final int CLIENT_MAIN_ARGS_LENGTH = 5;
    private static Registry registry;

    @BeforeAll
    static void startServer() {
        registry = BankTestUtils.startRMI();
    }

    private String[] convertToStringArray(final Object... args) {
        return Arrays.stream(args).map(Object::toString).toArray(String[]::new);
    }

    @Test
    void test1_incorrectArgsLength() {
        final int ARGS_LENGTH = 100;
        for (int length = 0; length < ARGS_LENGTH; length++) {
            if (length != CLIENT_MAIN_ARGS_LENGTH) {
                final int finalLength = length;
                assertThrows(ClientException.class, () -> Client.main(new String[finalLength]));
            }
        }
    }

    @Test
    void test2_incorrectArgsOrder() throws ServerException {
        newBank(registry);
        for (int i = 0; i < DATA_ARRAYS_LENGTH; i++) {
            final int finalI = i;
            assertThrows(ClientException.class,
                    () -> Client.main(convertToStringArray(PASSPORTS[finalI], NAMES[finalI], SURNAMES[finalI], ACCOUNTS[finalI], AMOUNTS[finalI])));
            assertThrows(ClientException.class,
                    () -> Client.main(convertToStringArray(AMOUNTS[finalI], PASSPORTS[finalI], SURNAMES[finalI], NAMES[finalI], ACCOUNTS[finalI])));
            assertThrows(ClientException.class,
                    () -> Client.main(convertToStringArray(SURNAMES[finalI], ACCOUNTS[finalI], PASSPORTS[finalI], AMOUNTS[finalI], NAMES[finalI])));
        }
    }

    @Test
    void test3_nullArgs() throws ServerException {
        newBank(registry);
        assertThrows(ClientException.class, () -> Client.main(null));
    }

    @Test
    void test4_registration() throws ServerException, ClientException, RemoteException {
        Bank bank = newBank(registry);
        for (int i = 0; i < DATA_ARRAYS_LENGTH; i++) {
            Client.main(convertToStringArray(NAMES[i], SURNAMES[i], PASSPORTS[i], ACCOUNTS[i], AMOUNTS[i]));
            Person person = bank.getRemotePerson(PASSPORTS[i]);
            assertNotNull(person);
            assertEquals(NAMES[i], person.getName());
            assertEquals(SURNAMES[i], person.getSurname());
            assertEquals(PASSPORTS[i], person.getPassport());
        }
    }

    @Test
    void test5_addingMoney() throws ServerException, ClientException, RemoteException {
        final int ADDING_COUNT = 1500;
        final int ADDING_AMOUNT = 50;
        Bank bank = newBank(registry);
        Client.main(convertToStringArray(NULL_STRING, NULL_STRING, 0, NULL_STRING, 0));
        for (int i = 0; i < ADDING_COUNT; i++) {
            Client.main(convertToStringArray(NULL_STRING, NULL_STRING, 0, NULL_STRING, ADDING_AMOUNT));
        }
        Person person = bank.getRemotePerson(0);
        assertEquals(ADDING_COUNT * ADDING_AMOUNT, person.getAccount(NULL_STRING).getAmount());
    }

}
