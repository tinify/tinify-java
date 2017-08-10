package com.tinify;

import okhttp3.Headers;

public class ResultMeta {
    protected final Headers meta;

    public ResultMeta(final Headers meta) {
        this.meta = meta;
    }

    public final Integer width() {
        String value = meta.get("image-width");
        return (value == null) ? null : Integer.parseInt(value);
    }

    public final Integer height() {
        String value = meta.get("image-height");
        return (value == null) ? null : Integer.parseInt(value);
    }

    public final String location() {
        return meta.get("location");
    }
}
