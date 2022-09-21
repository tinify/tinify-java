package com.tinify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import okhttp3.Headers;

public class Result extends ResultMeta {
    private final byte[] data;

    public Result(final Headers meta, final byte[] data) {
        super(meta);
        this.data = data;
    }

    public void toFile(final String path) throws IOException {
        Files.write(Paths.get(path), toBuffer());
    }

    public final byte[] toBuffer() {
        return data;
    }

    public final Integer size() {
        String value = meta.get("content-length");
        return (value == null) ? null : Integer.parseInt(value);
    }

    public final String mediaType() {
        return meta.get("content-type");
    }

    public final String extension() {
        return meta.get("content-type") == null ? null : meta.get("content-type").replaceFirst(".*/(.*)", "$1");
    }
}
