package ru.ifmo.rain.laptev.bank.client;

import ru.ifmo.rain.laptev.bank.common.Account;
import ru.ifmo.rain.laptev.bank.common.Bank;
import ru.ifmo.rain.laptev.bank.common.Person;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private int parseIntArgument(final String arg, final String argName) throws ClientException {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new ClientException(String.format("Incorrect %s", argName), e);
        }
    }

    private Bank newBank() throws ClientException, RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry();
            return  (Bank) registry.lookup("//localhost/bank");
        } catch (NotBoundException e) {
            throw new ClientException("Bank is not bound", e);
        }
    }

    private void validatePerson(final String name, final String surname, final Person person) throws ClientException, RemoteException {
        if (!person.getName().equals(name) || !person.getSurname().equals(surname)) {
            throw new ClientException("Account with this passport already exists");
        }
    }

    private void run(final String[] args) throws ClientException {
        if (args == null || args.length != 5) {
            throw new ClientException("Usage: <name> <surname> <passport> <account id> <amount>");
        }
        final String name = args[0];
        final String surname = args[1];
        final int passport = parseIntArgument(args[2], "passport");
        final String accountId = args[3];
        final int amount = parseIntArgument(args[4], "amount");
        try {
            final Bank bank = newBank();
            Person person = bank.getRemotePerson(passport);
            person = (person == null) ? bank.registerRemotePerson(name, surname, passport) : person;
            System.out.println("Account: " + person.getName() + " " + person.getSurname() + " " + person.getPassport());
            validatePerson(name, surname, person);

            Account account = person.getAccount(accountId);
            account = (account == null) ? person.createAccount(accountId) : account;
            System.out.println("Amount: " + account.getAmount());
            account.addAmount(amount);
            System.out.println("New amount: " + account.getAmount());

        } catch (RemoteException e) {
            throw new ClientException(String.format("Problem during executing remote method %s", e.getMessage()));
        }
    }

    public static void main(final String[] args) throws ClientException {
        new Client().run(args);
    }
}
