package com.tinify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Result extends ResultMeta {
    private final byte[] data;

    public Result(final Map<String, List<String>> meta, final byte[] data) {
        super(meta);
        this.data = data;
    }

    public void toFile(final String path) throws IOException {
        Files.write(Paths.get(path), toBuffer());
    }

    public final byte[] toBuffer() {
        return data;
    }

    public final int size() {
        return Integer.parseInt(meta.get("Content-Length").get(0));
    }

    public final String mediaType() {
        return meta.get("Content-Type").get(0);
    }
}
