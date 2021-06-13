package ru.ifmo.rain.laptev.bank.server;


class LocalAccount extends AbstractAccount {

    LocalAccount(final RemoteAccount account) {
        super(account.getId(), account.getAmount());
    }

    LocalAccount(final String id, final int amount) {
        super(id, amount);
    }
}
