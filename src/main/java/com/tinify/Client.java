package com.tinify;

import com.google.gson.Gson;
import com.squareup.okhttp.*;
import java.io.IOException;

public class Client {
    private OkHttpClient client;
    private String credentials;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    public static final String API_ENDPOINT = "https://api.tinify.com";
    public static final String USER_AGENT = "Tinify/"
            + Client.class.getPackage().getImplementationVersion()
            + " Java/" + System.getProperty("java.version")
            + " (" + System.getProperty("java.vendor")
            + ", " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + ")";

    public enum Method {
        POST,
        GET
    }

    public Client(final String key) {
        client = new OkHttpClient();
        credentials = Credentials.basic("api", key);
    }

    public final Response request(final Method method, final String endpoint) throws Exception {
        return request(method, endpoint, (RequestBody) null);
    }

    public final Response request(final Method method, final String endpoint, final Options options) throws Exception {
        return request(method, endpoint, RequestBody.create(JSON, options.toJson()));
    }

    public final Response request(final Method method, final String endpoint, final byte[] body) throws Exception {
        return request(method, endpoint, RequestBody.create(null, body));
    }

    private Response request(final Method method, final String endpoint, final RequestBody body) throws Exception {
        HttpUrl url;
        if (endpoint.startsWith("https")) {
            url = HttpUrl.parse(endpoint);
        } else {
            url = HttpUrl.parse(API_ENDPOINT + endpoint);
        }

        Request request = new Request.Builder()
                .header("Authorization", credentials)
                .header("User-Agent", USER_AGENT)
                .url(url)
                .method(method.toString(), body)
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new ConnectionException("Error while connecting: " + e.getMessage(), e);
        }

        String compressionCount = response.header("Compression-Count");
        if (compressionCount != null && !compressionCount.isEmpty()) {
            Tinify.setCompressionCount(Integer.valueOf(compressionCount));
        }

        if (response.isSuccessful()) {
            return response;
        } else {
            Exception.Data data;
            Gson gson = new Gson();
             try {
                 data = gson.fromJson(response.body().charStream(), Exception.Data.class);
            } catch (com.google.gson.JsonParseException e) {
                 data = new Exception().new Data();
                 data.setMessage("Error while parsing response: " + e.getMessage());
                 data.setError("ParseError");
            } catch (IOException e) {
                 throw new Exception(e);
             }
            throw Exception.create(
                    data.getMessage(),
                    data.getError(),
                    response.code());
        }
    }
}
