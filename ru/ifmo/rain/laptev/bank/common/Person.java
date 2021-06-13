package ru.ifmo.rain.laptev.bank.common;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote, Serializable {

    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    int getPassport() throws RemoteException;

    Account getAccount(final String id) throws RemoteException;

    Account createAccount(final String id) throws RemoteException;

}
