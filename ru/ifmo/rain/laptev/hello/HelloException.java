package ru.ifmo.rain.laptev.hello;

class HelloException extends RuntimeException {

    HelloException(final String message) {
        super(message);
    }

    HelloException(final String message, final Exception e) {
        super(message, e);
    }
}
