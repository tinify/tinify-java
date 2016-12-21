package com.tinify;

public class ClientException extends Exception {
    public ClientException() {
        super();
    }

    public ClientException(final Throwable t) {
        super(t);
    }

    public ClientException(final String message, final String type, final int status) {
        super(message, type, status);
    }
}
