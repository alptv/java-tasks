package ru.ifmo.rain.laptev.bank.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {

    Person getLocalPerson(final int passport) throws RemoteException;

    Person getRemotePerson(final int passport) throws RemoteException;

    Person registerRemotePerson(final String name, final String surname, final int passport) throws RemoteException;

    Person registerLocalPerson(final String name, final String surname, final int passport) throws RemoteException;

}
