package com.tinify;

import java.io.IOException;

public class Tinify {
    private static String key;
    private static int compressionCount = 0;
    private static Client client;

    public static Client client() {
        if (key == null) {
            throw new AccountException("Provide an API key with Tinify.setKey(...)");
        }
        if (client != null) {
            return client;
        } else {
            synchronized(Tinify.class) {
                client = new Client(key());
            }
            return client;
        }
    }

    public static void setKey(final String key) {
        Tinify.key = key;
        client = null;
    }

    public static Source fromFile(final String path) throws IOException {
        return Source.fromFile(path);
    }

    public static Source fromBuffer(final byte[] buffer) {
        return Source.fromBuffer(buffer);
    }

    public static boolean validate() {
        try {
            client().request(Client.Method.POST, "/", new Options.Builder().build());
        } catch (ClientException ex) {
            return true;
        }
        return false;
    }

    public static String key() {
        return key;
    }

    public static void setCompressionCount(final int count) {
        compressionCount = count;
    }

    public static int compressionCount() {
        return compressionCount;
    }
}