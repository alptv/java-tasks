package ru.ifmo.rain.laptev.bank.server;


public class ServerException extends Exception {
    ServerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}