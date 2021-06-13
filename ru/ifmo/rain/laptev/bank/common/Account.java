package ru.ifmo.rain.laptev.bank.common;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Account extends Remote, Serializable {

    String getId() throws RemoteException;

    int getAmount() throws RemoteException;

    void setAmount(final int amount) throws RemoteException;

    void addAmount(final int additiveAmount) throws RemoteException;
}
