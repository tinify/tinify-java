package com.tinify;

import com.google.gson.Gson;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import okhttp3.HttpUrl;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ClientTest {
    Client subject;
    MockWebServer server;
    String key = "key";

    @Before
    public void setup() throws IOException {
        Logger.getLogger(MockWebServer.class.getName()).setLevel(Level.WARNING);

        server = new MockWebServer();
        server.start();
        subject = new Client(key);
        new MockUp<HttpUrl>()
        {
            @Mock
            @SuppressWarnings("unused")
            HttpUrl parse(Invocation inv, String url)
            {
                if (url.contains("localhost")) {
                    return inv.proceed();
                } else {
                    return new HttpUrl.Builder()
                            .scheme("http")
                            .host(server.getHostName())
                            .port(server.getPort())
                            .encodedPath("/shrink")
                            .build();
                }
            }
        };
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    public void enqueuShrink() {
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/foo.png")
                .addHeader("Compression-Count", 12));
    }

    @Test
    public void requestWhenValidShouldIssueRequest() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink");
        String credentials = new String(Base64.encodeBase64(("api:" + key).getBytes()));

        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals("Basic " + credentials, request.getHeader("Authorization"));
    }

    @Test
    public void requestWhenValidShouldIssueRequestToEndpoint() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink");
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals("/shrink", request.getPath());
    }

    @Test
    public void requestWhenValidShouldIssueRequestWithMethod() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink");
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals("POST", request.getMethod());
    }

    @Test
    public void requestWhenValidShouldReturnResponse() throws Exception, InterruptedException, IOException, URISyntaxException {
        enqueuShrink();

        byte[] body = Files.readAllBytes(
                Paths.get(getClass().getResource("/voormedia.png").toURI()));

        Client.Response response = subject.request(Client.Method.POST, "/shrink", body);
        assertEquals("https://api.tinify.com/foo.png", response.headers.get("Location"));
    }

    @Test
    public void requestWhenValidShouldIssueRequestWithoutBodyWhenOptionsAreEmpty() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.GET, "/shrink", new Options());
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals(0, request.getBody().size());
    }

    @Test
    public void requestWhenValidShouldIssueRequestWithoutContentTypeWhenOptionsAreEmpty() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.GET, "/shrink", new Options());
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals(null, request.getHeader("Content-Type"));
    }

    @Test
    public void requestWhenValidShouldIssueRequestWithJSONBody() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink", new Options().with("hello", "world"));
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        Gson gson = new Gson();
        assertEquals("world", gson.fromJson(request.getBody().readUtf8(), HashMap.class).get("hello"));
        assertEquals("application/json; charset=utf-8", request.getHeader("Content-Type"));
    }

    @Test
    public void requestWhenValidShouldIssueRequestWithUserAgent() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink", new byte[] {});
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals(Client.USER_AGENT, request.getHeader("User-Agent"));
    }

    @Test
    public void requestWhenValidShouldUpdateCompressionCount() throws Exception {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink", new byte[] {});
        assertEquals(12, Tinify.compressionCount());
    }

    @Test
    public void requestWhenValidWithAppIdShouldIssueRequestWithUserAgent() throws Exception, InterruptedException {
        enqueuShrink();
        Client client = new Client(key, "TestApp/0.1");
        client.request(Client.Method.POST, "/shrink");
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals(Client.USER_AGENT + " TestApp/0.1", request.getHeader("User-Agent"));
    }

    @Test
    public void requestWhenValidWithProxyShouldIssueRequestWithProxyAuthorization() throws Exception, InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(407));
        enqueuShrink();
        Client client = new Client(key, null, "http://user:pass@" + server.getHostName() + ":" + server.getPort());
        client.request(Client.Method.POST, "/shrink");
        RecordedRequest request1 = server.takeRequest(5, TimeUnit.SECONDS);
        RecordedRequest request2 = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals("Basic dXNlcjpwYXNz", request2.getHeader("Proxy-Authorization"));
    }

    @Test
    public <T extends Call> void requestWithTimeoutOnceShouldReturnResponse() throws Exception {
        new MockUp<T>() {
            int count = 1;

            @Mock
            public Response execute(Invocation inv) throws IOException {
                if (count == 0) {
                    return inv.proceed();
                } else {
                    count--;
                    throw new java.net.SocketTimeoutException("SocketTimeoutException");
                }
            }
        };

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("ok"));

        Client.Response response = new Client(key).request(Client.Method.POST, "/shrink");
        assertEquals("ok", new String(response.body));
    }

    @Test(expected = ConnectionException.class)
    public <T extends Call> void requestWithTimeoutRepeatedlyShouldThrowConnectionException() throws Exception {
        new MockUp<T>() {
            @Mock
            public Response execute(Invocation inv) throws IOException {
                throw new java.net.SocketTimeoutException("SocketTimeoutException");
            }
        };

        new Client(key).request(Client.Method.POST, "http://shrink");
    }

    @Test
    public <T extends Call> void requestWithTimeoutRepeatedlyShouldThrowExceptionWithMessage() throws Exception {
        new MockUp<T>() {
            @Mock
            public Response execute(Invocation inv) throws IOException {
                throw new java.net.SocketTimeoutException("SocketTimeoutException");
            }
        };

        try {
            new Client(key).request(Client.Method.POST, "http://shrink");
            fail("Expected an Exception to be thrown");
        } catch (ConnectionException e) {
            assertEquals("Error while connecting: SocketTimeoutException", e.getMessage());
        }
    }

    @Test
    public <T extends Call> void requestWithSocketErrorOnceShouldReturnResponse() throws Exception {
        new MockUp<T>() {
            int count = 1;

            @Mock
            public Response execute(Invocation inv) throws IOException {
                if (count == 0) {
                    return inv.proceed();
                } else {
                    count--;
                    throw new java.net.UnknownHostException("UnknownHostException");
                }
            }
        };

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("ok"));

        Client.Response response = new Client(key).request(Client.Method.POST, "/shrink");
        assertEquals("ok", new String(response.body));
    }

    @Test(expected = ConnectionException.class)
    public <T extends Call> void requestWithSocketErrorRepeatedlyShouldThrowConnectionException() throws Exception {
        new MockUp<T>() {
            @Mock
            public Response execute(Invocation inv) throws IOException {
                throw new java.net.UnknownHostException("UnknownHostException");
            }
        };

        new Client(key).request(Client.Method.POST, "http://shrink");
    }

    @Test
    public <T extends Call> void requestWithSocketErrorRepeatedlyShouldThrowExceptionWithMessage() throws Exception {
        new MockUp<T>() {
            @Mock
            public Response execute(Invocation inv) throws IOException {
                throw new java.net.UnknownHostException("UnknownHostException");
            }
        };

        try {
            new Client(key).request(Client.Method.POST, "http://shrink");
            fail("Expected an Exception to be thrown");
        } catch (ConnectionException e) {
            assertEquals("Error while connecting: UnknownHostException", e.getMessage());
        }
    }

    @Test
    public <T extends Call> void requestWithUnexpectedExceptionOnceShouldReturnResponse() throws Exception {
        new MockUp<T>() {
            int count = 1;

            @Mock
            public Response execute(Invocation inv) throws IOException {
                if (count == 0) {
                    return inv.proceed();
                } else {
                    count--;
                    throw new RuntimeException("Some exception");
                }
            }
        };

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("ok"));

        Client.Response response = new Client(key).request(Client.Method.POST, "/shrink");
        assertEquals("ok", new String(response.body));
    }

    @Test(expected = ConnectionException.class)
    public <T extends Call> void requestWithUnexpectedExceptionRepeatedlyShouldThrowConnectionException() throws Exception {
        new MockUp<T>() {
            @Mock
            public Response execute(Invocation inv) throws IOException {
                throw new RuntimeException("Some exception");
            }
        };

        new Client(key).request(Client.Method.POST, "http://shrink");
    }

    @Test
    public <T extends Call> void requestWithUnexpectedExceptionRepeatedlyShouldThrowExceptionWithMessage() throws Exception {
        new MockUp<T>() {
            @Mock
            public Response execute(Invocation inv) throws IOException {
                throw new RuntimeException("Some exception");
            }
        };

        try {
            new Client(key).request(Client.Method.POST, "http://shrink");
            fail("Expected an Exception to be thrown");
        } catch (ConnectionException e) {
            assertEquals("Error while connecting: Some exception", e.getMessage());
        }
    }

    @Test
    public void requestWithServerErrorOnceShouldReturnResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(584)
                .setBody("{'error':'InternalServerError','message':'Oops!'}"));
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("ok"));

        Client.Response response = new Client(key).request(Client.Method.POST, "/shrink");
        assertEquals("ok", new String(response.body));
    }

    @Test(expected = ServerException.class)
    public void requestWithServerErrorRepeatedlyShouldThrowServerException() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(584)
                .setBody("{'error':'InternalServerError','message':'Oops!'}"));
        server.enqueue(new MockResponse()
                .setResponseCode(584)
                .setBody("{'error':'InternalServerError','message':'Oops!'}"));

        new Client(key).request(Client.Method.POST, "/shrink");
    }

    @Test
    public void requestWithServerErrorRepeatedlyShouldThrowExceptionWithMessage() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(584)
                .setBody("{'error':'InternalServerError','message':'Oops!'}"));
        server.enqueue(new MockResponse()
                .setResponseCode(584)
                .setBody("{'error':'InternalServerError','message':'Oops!'}"));

        try {
            new Client(key).request(Client.Method.POST, "/shrink");
            fail("Expected an Exception to be thrown");
        } catch (Exception e) {
            assertEquals("Oops! (HTTP 584/InternalServerError)", e.getMessage());
        }
    }

    @Test
    public void requestWithBadServerResponseOnceShouldReturnResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(543)
                .setBody("<!-- this is not json -->"));
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("ok"));

        Client.Response response = new Client(key).request(Client.Method.POST, "/shrink");
        assertEquals("ok", new String(response.body));
    }

    @Test(expected = ServerException.class)
    public void requestWithBadServerResponseRepeatedlyShouldThrowServerException() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(543)
                .setBody("<!-- this is not json -->"));
        server.enqueue(new MockResponse()
                .setResponseCode(543)
                .setBody("<!-- this is not json -->"));

        new Client(key).request(Client.Method.POST, "/shrink");
    }

    @Test
    public void requestWithBlankServerResponseRepeatedlyShouldThrowServerException() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(543)
                .setBody(""));
        server.enqueue(new MockResponse()
                .setResponseCode(543)
                .setBody(""));
        try {
            new Client(key).request(Client.Method.POST, "/shrink");
            fail("Expected an Exception to be thrown");
        } catch (Exception e) {
            assertEquals("Error while parsing response: received empty body (HTTP 543/ParseError)", e.getMessage());
        }
    }

    @Test
    public void requestWithBadServerResponseRepeatedlyShouldThrowExceptionWithMessage() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(543)
                .setBody("<!-- this is not json -->"));
        server.enqueue(new MockResponse()
                .setResponseCode(543)
                .setBody("<!-- this is not json -->"));

        try {
            new Client(key).request(Client.Method.POST, "/shrink");
            fail("Expected an Exception to be thrown");
        } catch (Exception e) {
            assertEquals("Error while parsing response: java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path $ (HTTP 543/ParseError)", e.getMessage());
        }
    }

    @Test(expected = ClientException.class)
    public void requestWithClientErrorShouldThrowClientException() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(492)
                .setBody("{'error':'BadRequest','message':'Oops!'}"));

        new Client(key).request(Client.Method.POST, "/shrink");
    }

    @Test
    public void requestWithClientErrorShouldThrowExceptionWithMessage() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(492)
                .setBody("{'error':'BadRequest','message':'Oops!'}"));

        try {
            new Client(key).request(Client.Method.POST, "/shrink");
            fail("Expected an Exception to be thrown");
        } catch (Exception e) {
            assertEquals("Oops! (HTTP 492/BadRequest)", e.getMessage());
        }
    }

    @Test(expected = AccountException.class)
    public void requestWithBadCredentialsShouldThrowAccountException() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{'error':'Unauthorized','message':'Oops!'}"));

        new Client(key).request(Client.Method.POST, "/shrink");
    }

    @Test
    public void requestWithBadCredentialsShouldThrowExceptionWithMessage() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{'error':'Unauthorized','message':'Oops!'}"));

        try {
            new Client(key).request(Client.Method.POST, "/shrink");
            fail("Expected an Exception to be thrown");
        } catch (Exception e) {
            assertEquals("Oops! (HTTP 401/Unauthorized)", e.getMessage());
        }
    }
}
