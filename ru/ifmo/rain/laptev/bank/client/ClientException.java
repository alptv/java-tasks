package ru.ifmo.rain.laptev.bank.client;

public class ClientException extends Exception {

    ClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    ClientException(final String message) {
        super(message);
    }
}