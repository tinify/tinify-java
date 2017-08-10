package com.tinify;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.Route;

import java.io.IOException;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Client {
    public class Response {
        public Headers headers;
        public byte[] body;

        public Response(Headers headers, byte[] body) {
            this.headers = headers;
            this.body = body;
        }
    }

    private OkHttpClient client;
    private String credentials;
    private String userAgent;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static final String API_ENDPOINT = "https://api.tinify.com";

    public static final short RETRY_COUNT = 1;
    public static final short RETRY_DELAY = 500;

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
        this(key, null, null);
    }

    public Client(final String key, final String appIdentifier) {
        this(key, appIdentifier, null);
    }

    public Client(final String key, final String appIdentifier, final String proxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (proxy != null) {
            try {
                URL url = new URL(proxy);
                Proxy proxyAddress = createProxyAddress(url);
                Authenticator proxyAuthenticator = createProxyAuthenticator(url);

                if (proxyAddress != null) {
                    builder.proxy(proxyAddress);
                    if (proxyAuthenticator != null) {
                        builder.proxyAuthenticator(proxyAuthenticator);
                    }
                }
            } catch (java.lang.Exception e) {
                throw new ConnectionException("Invalid proxy: " + e.getMessage(), e);
            }
        }

        builder.sslSocketFactory(TLSContext.socketFactory, TLSContext.trustManager);
        builder.connectTimeout(0, TimeUnit.SECONDS);
        builder.readTimeout(0, TimeUnit.SECONDS);
        builder.writeTimeout(0, TimeUnit.SECONDS);

        client = builder.build();

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
        /* OkHttp does not support null request bodies if the method is POST. */
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
            @Override public Request authenticate(Route route, okhttp3.Response response) throws IOException {
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

        for (short retries = RETRY_COUNT; retries >= 0; retries--) {
            if (retries < RETRY_COUNT) {
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            Request request = new Request.Builder()
                    .header("Authorization", credentials)
                    .header("User-Agent", userAgent)
                    .url(url)
                    .method(method.toString(), body)
                    .build();

            Response response;
            int status;

            try {
                okhttp3.Response res = client.newCall(request).execute();
                status = res.code();
                response = new Response(res.headers(), res.body().bytes());
            } catch (java.lang.Exception e) {
                if (retries > 0) continue;
                throw new ConnectionException("Error while connecting: " + e.getMessage(), e);
            }

            String compressionCount = response.headers.get("Compression-Count");
            if (compressionCount != null && !compressionCount.isEmpty()) {
                Tinify.setCompressionCount(Integer.valueOf(compressionCount));
            }

            if (status >= 200 && status < 300) {
                return response;
            }

            Exception.Data data;
            Gson gson = new Gson();
            try {
                data = gson.fromJson(new String(response.body), Exception.Data.class);
                if (data == null) {
                    data = new Exception.Data();
                    data.setMessage("Error while parsing response: received empty body");
                    data.setError("ParseError");
                }
            } catch (com.google.gson.JsonParseException e) {
                data = new Exception.Data();
                data.setMessage("Error while parsing response: " + e.getMessage());
                data.setError("ParseError");
            }

            if (retries > 0 && status >= 500) continue;
            throw Exception.create(
                    data.getMessage(),
                    data.getError(),
                    status);
        }

        return null;
    }
}
