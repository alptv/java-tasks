package ru.ifmo.rain.laptev.bank.server;

import ru.ifmo.rain.laptev.bank.common.Bank;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;


public class Server {
    private static final int BANK_PORT = 8888;
    private static final String ADDRESS = "//localhost/bank";

    private void run() throws ServerException {
        final Bank bank = new RemoteBank(BANK_PORT);
        final Registry registry;
        try {
            UnicastRemoteObject.exportObject(bank, BANK_PORT);
            registry = LocateRegistry.getRegistry();
            registry.rebind(ADDRESS, bank);
        } catch (final RemoteException e) {
            throw new ServerException("Can't export bank " + e.getMessage(), e);
        }
    }

    public static void main(final String[] args) throws ServerException {
        new Server().run();
    }
}

