package com.tinify;

import com.google.gson.Gson;
import com.squareup.okhttp.*;
import java.io.IOException;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Client {
    private OkHttpClient client;
    private String credentials;
    private String userAgent;

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

    public Client(final String key, final String appIdentifier, final String proxy) {
        client = new OkHttpClient();

        if (proxy != null) {
            URL proxyUrl;
            try {
                proxyUrl = new URL(proxy);
            } catch(java.net.MalformedURLException error) {
                throw new Exception(error.getMessage());
            }

            String proxyHost = proxyUrl.getHost();
            int proxyPort = proxyUrl.getPort();
            if (proxyPort < 0) { proxyPort = 80; }
            Proxy proxyInstance = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            client.setProxy(proxyInstance);
        }
        client.setSslSocketFactory(SSLContext.getSocketFactory());
        client.setConnectTimeout(0, TimeUnit.SECONDS);
        client.setReadTimeout(0, TimeUnit.SECONDS);
        client.setWriteTimeout(0, TimeUnit.SECONDS);

        credentials = Credentials.basic("api", key);
        if (appIdentifier == null) {
            userAgent = USER_AGENT;
        } else {
            userAgent = USER_AGENT + " " + appIdentifier;
        }
    }

    public final Response request(final Method method, final String endpoint) throws Exception {
        /* OkHttp does not support null request bodies if the method is POST. */
        if (method.equals(Method.POST)) {
            return request(method, endpoint, RequestBody.create(null, new byte[] {}));
        } else {
            return request(method, endpoint, (RequestBody) null);
        }
    }

    public final Response request(final Method method, final String endpoint, final Options options) throws Exception {
        if (method.equals(Method.GET)) {
            return request(method, endpoint, options.isEmpty() ? null : RequestBody.create(JSON, options.toJson()));
        } else {
            return request(method, endpoint, RequestBody.create(JSON, options.toJson()));
        }
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
                .header("User-Agent", userAgent)
                .url(url)
                .method(method.toString(), body)
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (java.lang.Exception e) {
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
                 if (data == null) {
                     data = new Exception.Data();
                     data.setMessage("Error while parsing response: received empty body");
                     data.setError("ParseError");
                 }
            } catch (com.google.gson.JsonParseException e) {
                 data = new Exception.Data();
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
