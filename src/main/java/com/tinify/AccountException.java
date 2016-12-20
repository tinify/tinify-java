package com.tinify;

public class AccountException extends Exception {
    public AccountException() {
        super();
    }

    public AccountException(final Throwable t) {
        super(t);
    }

    public AccountException(final String message) {
        super(message);
    }

    public AccountException(final String message, final String type, final int status) {
        super(message, type, status);
    }
}
