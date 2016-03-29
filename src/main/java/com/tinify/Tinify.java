package com.tinify;

import java.io.IOException;

public class Tinify {
    private static String key;
    private static String appIdentifier;
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
                if (client == null) {
                    client = new Client(key(), appIdentifier());
                }
            }
            return client;
        }
    }

    public static void setKey(final String key) {
        Tinify.key = key;
        client = null;
    }

    public static void setAppIdentifier(final String identifier) {
        Tinify.appIdentifier = identifier;
        client = null;
    }

    public static Source fromFile(final String path) throws IOException {
        return Source.fromFile(path);
    }

    public static Source fromBuffer(final byte[] buffer) {
        return Source.fromBuffer(buffer);
    }

    public static Source fromUrl(final String url) {
        return Source.fromUrl(url);
    }

    public static boolean validate() {
        try {
            client().request(Client.Method.POST, "/shrink");
        } catch (ClientException ex) {
            return true;
        }
        return false;
    }

    public static String key() {
        return key;
    }

    public static String appIdentifier() {
        return appIdentifier;
    }

    public static void setCompressionCount(final int count) {
        compressionCount = count;
    }

    public static int compressionCount() {
        return compressionCount;
    }
}
