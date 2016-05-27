package com.tinify;

public class Exception extends RuntimeException {
    public static class Data {
        private String message;
        private String error;

        public void setMessage(final String message) {
            this.message = message;
        }

        public final String getMessage() {
            return message;
        }

        public void setError(final String error) {
            this.error = error;
        }

        public final String getError() {
            return error;
        }
    }

    public static Exception create(final String message, final String type, final int status) {
        if (status == 401 || status == 429) {
            return new AccountException(message, type, status);
        } else if (status >= 400 && status <= 499) {
            return new ClientException(message, type, status);
        } else if (status >= 500 && status <= 599) {
            return new ServerException(message, type, status);
        } else {
            return new Exception(message, type, status);
        }
    }

    int status = 0;

    public Exception() {
        super();
    }

    public Exception(final Throwable t) {
        super(t);
    }

    public Exception(final String message) {
        super(message);
    }

    public Exception(final String message, final Throwable t) {
        super(message, t);
    }

    public Exception(final String message, final String type, final int status) {
        super(message + " (HTTP " + status + "/" + type + ")");
        this.status = status;
    }
}
