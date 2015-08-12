package com.tinify;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public final class Options {
    private Map<String, Object> options;

    private Options(Builder builder) {
        this.options = builder.options;
    }

    public final String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this.options);
    }

    public static class Builder {
        private Map<String, Object> options;

        public Builder() {
            options = new HashMap<>();
        }

        public Builder(Options options) {
            this.options = options.options;
        }

        public Builder add(final String key, final Options options) {
            this.options.put(key, options.options);
            return this;
        }

        public Builder add(final String key, final Object value) {
            options.put(key, value);
            return this;
        }

        public Options build() {
            return new Options(this);
        }
    }
}
