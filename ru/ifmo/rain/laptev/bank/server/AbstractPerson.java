package ru.ifmo.rain.laptev.bank.server;

import ru.ifmo.rain.laptev.bank.common.Account;
import ru.ifmo.rain.laptev.bank.common.Person;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractPerson<E extends AbstractAccount> implements Person {
    private final String name;
    private final String surname;
    private final int passport;

    private final Map<String, E> accounts;

    AbstractPerson(final String name, final String surname, final int passport) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.accounts = new ConcurrentHashMap<>();
    }


    AbstractPerson(final String name,
                   final String surname,
                   final int passport,
                   final Map<String, E> accounts) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.accounts = accounts;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public int getPassport() {
        return passport;
    }

    @Override
    public Account getAccount(final String id) {
        return accounts.get(id);
    }

    Map<String, E> getAccounts() {
        return accounts;
    }
}
