package com.tinify;

import java.util.Map;
import java.util.List;

public class ResultMeta {
    protected final Map<String, List<String>> meta;

    public ResultMeta(final Map<String, List<String>> meta) {
        this.meta = meta;
    }

    public final Integer width() {
        String value = tryValue("Image-Width");
        return (value == null) ? null : Integer.parseInt(value);
    }

    public final Integer height() {
        String value = tryValue("Image-Height");
        return (value == null) ? null : Integer.parseInt(value);
    }

    public final String location() {
        return tryValue("Location");
    }

    protected final String tryValue(final String key) {
        List<String> values = meta.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        } else {
            return values.get(0);
        }
    }
}
