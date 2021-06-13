package ru.ifmo.rain.laptev.bank.server;

import ru.ifmo.rain.laptev.bank.common.Account;

abstract class AbstractAccount implements Account {
    private final String id;
    private int amount;

    AbstractAccount(final String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public synchronized int getAmount() {
        return amount;
    }

    public synchronized void setAmount(final int amount) {
        this.amount = amount;
    }

    public synchronized void addAmount(final int additiveAmount)  {
        this.amount += additiveAmount;
    }

}
