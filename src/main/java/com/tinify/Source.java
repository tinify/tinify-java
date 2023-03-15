package com.tinify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Source {
    private String url;
    private Options commands;

    public static Source fromFile(final String path) throws IOException {
        return fromBuffer(Files.readAllBytes(Paths.get(path)));
    }

    public static Source fromBuffer(final byte[] buffer) {
        Client.Response response = Tinify.client().request(Client.Method.POST, "/shrink", buffer);
        return new Source(response.headers.get("location"), new Options());
    }

    public static Source fromUrl(final String url) {
        Options body = new Options().with("source", new Options().with("url", url));
        Client.Response response = Tinify.client().request(Client.Method.POST, "/shrink", body);
        return new Source(response.headers.get("location"), new Options());
    }

    public Source(final String url, final Options commands) {
        this.url = url;
        this.commands = commands;
    }

    public final Source preserve(final String... options) {
        return new Source(url, new Options(commands).with("preserve", options));
    }

    public final Source resize(final Options options) {
        return new Source(url, new Options(commands).with("resize", options));
    }

    public final Source convert(final Options options) {
        return new Source(url, new Options(commands).with("convert", options));
    }

    public final Source transform(final Options options) {
        return new Source(url, new Options(commands).with("transform", options));
    }

    public final ResultMeta store(final Options options) {
        Options params = new Options(commands).with("store", options);
        Client.Response response = Tinify.client().request(Client.Method.POST, url, params);
        return new ResultMeta(response.headers);
    }

    public final Result result() throws IOException {
        Client.Response response;
        if (commands == null || commands.isEmpty()) {
            response = Tinify.client().request(Client.Method.GET, url);
        } else {
            response = Tinify.client().request(Client.Method.POST, url, commands);
        }

        /* No need for try(Response response = ...): body().bytes() calls close(). */
        return new Result(response.headers, response.body);
    }

    public void toFile(final String path) throws IOException {
        result().toFile(path);
    }

    public final byte[] toBuffer() throws IOException {
        return result().toBuffer();
    }
}
