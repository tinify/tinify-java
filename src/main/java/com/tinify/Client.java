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

    public Client(final String key, final String appIdentifier, final URL proxy) {
        client = new OkHttpClient();

        Proxy proxyAddress = createProxyAddress(proxy);
        Authenticator proxyAuthenticator = createProxyAuthenticator(proxy);

        if (proxyAddress != null) {
            client.setProxy(proxyAddress);
            if (proxyAuthenticator != null) {
                client.setAuthenticator(proxyAuthenticator);
            }
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

    private Proxy createProxyAddress(final URL proxy) {
        if (proxy == null) return null;

        String host = proxy.getHost();
        int port = proxy.getPort();

        if (port < 0) {
            port = proxy.getDefaultPort();
        }

        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    private Authenticator createProxyAuthenticator(final URL proxy) {
        if (proxy == null) return null;

        String user = proxy.getUserInfo();
        if (user == null) return null;

        final String username, password;
        int c = user.indexOf(':');
        if (0 < c) {
            username = user.substring(0, c);
            password = user.substring(c + 1);
        } else {
            username = user;
            password = null;
        }

        return new Authenticator() {
            @Override public Request authenticate(Proxy proxy, Response response) throws IOException {
                return null;
            }

            @Override public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                String credential = Credentials.basic(username, password);
                return response.request().newBuilder().header("Proxy-Authorization", credential).build();
            }
        };
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
