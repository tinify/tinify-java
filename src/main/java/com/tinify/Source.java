package com.tinify;

import com.squareup.okhttp.Response;

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
        Response response = Tinify.client().request(Client.Method.POST, "/shrink", buffer);
        return new Source(response.header("location"), new Options());
    }

    public static Source fromUrl(final String url) {
        Options body = new Options().with("source", new Options().with("url", url));
        Response response = Tinify.client().request(Client.Method.POST, "/shrink", body);
        return new Source(response.header("location"), new Options());
    }

    public Source(final String url, final Options commands) {
        this.url = url;
        this.commands = commands;
    }

    public final Source resize(Options options) {
        return new Source(url, new Options(commands).with("resize", options));
    }

    public final ResultMeta store(Options options) {
        Response response = Tinify.client().request(
                Client.Method.POST, url, new Options(commands).with("store", options));
        return new ResultMeta(response.headers());
    }

    public final Result result() throws IOException {
        Response response;
        if (commands == null) {
            response = Tinify.client().request(Client.Method.GET, url);
        } else {
            response = Tinify.client().request(Client.Method.POST, url, commands);
        }
        return new Result(response.headers(), response.body().bytes());
    }

    public void toFile(final String path) throws IOException {
        result().toFile(path);
    }

    public final byte[] toBuffer() throws IOException {
        return result().toBuffer();
    }
}
