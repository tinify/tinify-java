package com.tinify;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public final class Options {
    private Map<String, Object> options;

    public Options() {
        this.options = new HashMap<>();
    }

    public Options(Options options) {
        this.options = new HashMap<>(options.options);
    }

    public Options with(final String key, final Object value) {
        this.options.put(key, value);
        return this;
    }

    public Options with(final String key, final Options options) {
        this.options.put(key, options.options);
        return this;
    }

    public final String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this.options);
    }

    public boolean isEmpty() {
        return this.options.isEmpty();
    }
}
