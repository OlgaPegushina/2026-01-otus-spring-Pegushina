package ru.otus.hw.exceptions;

public class NumberAttemptsException extends RuntimeException {
    public NumberAttemptsException(String message, Throwable ex) {
        super(message, ex);
    }

    public NumberAttemptsException(String message) {
        super(message);
    }

    public NumberAttemptsException() {

    }
}
