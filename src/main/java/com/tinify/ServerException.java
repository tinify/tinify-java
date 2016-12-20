package com.tinify;

public class ServerException extends Exception {
    public ServerException() {
        super();
    }

    public ServerException(final Throwable t) {
        super(t);
    }

    public ServerException(final String message, final String type, final int status) {
        super(message, type, status);
    }
}
