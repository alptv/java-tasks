package ru.ifmo.rain.laptev.bank.server;


import ru.ifmo.rain.laptev.bank.common.Bank;
import ru.ifmo.rain.laptev.bank.common.Person;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

class RemoteBank implements Bank {
    private final Map<Integer, RemotePerson> persons;
    private int port;

    RemoteBank(final int port) {
        this.persons = new ConcurrentHashMap<>();
        this.port = port;
    }

    @Override
    public LocalPerson getLocalPerson(final int passport) {
        RemotePerson person = persons.get(passport);
        return person == null ? null : new LocalPerson(person);
    }

    @Override
    public RemotePerson getRemotePerson(final int passport) {
        return persons.get(passport);
    }

    @Override
    public Person registerRemotePerson(final String name, final String surname, final int passport) throws RemoteException {
        return registerPerson(name, surname, passport, person -> person);
    }

    @Override
    public Person registerLocalPerson(final String name, final String surname, final int passport) throws RemoteException {
        return registerPerson(name, surname, passport, LocalPerson::new);
    }

    private Person registerPerson(final String name, final String surname, final int passport, Function<RemotePerson, Person> mapper) throws RemoteException {
        final RemotePerson person = new RemotePerson(name, surname, passport, port);
        if (persons.putIfAbsent(passport, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return mapper.apply(person);
        }
        return null;
    }
}
