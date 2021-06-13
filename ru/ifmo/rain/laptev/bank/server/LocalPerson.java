package ru.ifmo.rain.laptev.bank.server;


import ru.ifmo.rain.laptev.bank.common.Account;

import java.util.Map;
import java.util.stream.Collectors;

class LocalPerson extends AbstractPerson<LocalAccount> {
    LocalPerson(final RemotePerson person) {
        super(person.getName(), person.getSurname(), person.getPassport(),
                person.getAccounts().entrySet().stream()
                        .collect(
                                Collectors.toMap(Map.Entry::getKey, entry -> new LocalAccount(entry.getValue()))
                        ));
    }

    @Override
    public Account createAccount(final String id) {
        LocalAccount account = new LocalAccount(id, 0);
        if (getAccounts().putIfAbsent(id, account) == null) {
            return account;
        }
        return null;
    }
}
