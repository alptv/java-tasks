package ru.ifmo.rain.laptev.bank.server;


import ru.ifmo.rain.laptev.bank.common.Account;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

class RemotePerson extends AbstractPerson<RemoteAccount> {
    private final int port;

    RemotePerson(final String name, final String surname, final int passport, final int port) {
        super(name, surname, passport);
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        RemoteAccount account = new RemoteAccount(id, 0);
        if (getAccounts().putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        }
        return null;

    }
}
