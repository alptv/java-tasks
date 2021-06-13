package ru.ifmo.rain.laptev.walk;

import java.io.IOException;

class WalkerException extends IOException {
    WalkerException(String message, Exception e) {
        super(message, e);
    }
    WalkerException(String message) {
        super(message);
    }
}
