package com.tinify;

public class ConnectionException extends Exception {
    public ConnectionException() {
        super();
    }

    public ConnectionException(final Throwable t) {
        super(t);
    }

    public ConnectionException(final String message, final Throwable t) { super(message, t); }
}
