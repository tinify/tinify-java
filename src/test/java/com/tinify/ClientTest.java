package com.tinify;

import com.google.gson.Gson;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import mockit.*;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.*;
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
    String key = "ABC123456789";

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
            HttpUrl parse(String input)
            {
                return new HttpUrl.Builder()
                        .scheme("http")
                        .host(server.getHostName())
                        .port(server.getPort())
                        .encodedPath("/shrink")
                        .build();
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
                .addHeader("Location", "https://api.tinify.com/output/3spbi1cd7rs812lb.png")
                .addHeader("Compression-Count", 12));
    }

    @Test
    public void requestShouldSendApiKey() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink", new byte[] {});
        String credentials = new String(Base64.encodeBase64(("api:" + key).getBytes()));

        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals("Basic " + credentials, request.getHeader("Authorization"));
    }

    @Test
    public void requestShouldSendJSONBody() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink", new Options.Builder().add("hello", "world").build());
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        Gson gson = new Gson();
        assertEquals("world", gson.fromJson(request.getBody().readUtf8(), HashMap.class).get("hello"));
        assertEquals("application/json; charset=utf-8", request.getHeader("Content-Type"));
    }

    @Test
    public void requestShouldSendUserAgent() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink", new byte[]{});
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals(Client.USER_AGENT, request.getHeader("User-Agent"));
    }

    @Test
    public void requestShouldSetCompressionCount() throws Exception {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink", new byte[]{});
        assertEquals(12, Tinify.compressionCount());
    }

    @Test
    public void requestShouldCallEndpoint() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink", new byte[] {});
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals("/shrink", request.getPath());
    }

    @Test
    public void requestShouldCallWithMethod() throws Exception, InterruptedException {
        enqueuShrink();
        subject.request(Client.Method.POST, "/shrink", new byte[] {});
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals("POST", request.getMethod());
    }

    @Test
    public void requestShouldReturnResponse() throws Exception, InterruptedException, IOException {
        enqueuShrink();

        byte[] body = Files.readAllBytes(
                Paths.get(getClass().getResource("/example.png").getFile()));

        assertEquals("https://api.tinify.com/output/3spbi1cd7rs812lb.png",
                subject.request(Client.Method.POST, "/shrink", body).header("Location"));
    }

    @Test(expected = ServerException.class)
    public void requestShouldThrowServerException() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(584)
                .setBody("{'error':'InternalServerError','message':'Oops!'}"));
        new Client(key).request(Client.Method.POST, "/shrink", new byte[] {});
    }

    @Test
    public void requestShouldHaveMessageForServerException() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(584)
                .setBody("{'error':'InternalServerError','message':'Oops!'}"));
        try {
            new Client(key).request(Client.Method.POST, "/shrink", new byte[] {});
            fail("Expected an Exception to be thrown");
        } catch (Exception e) {
            assertEquals("Oops! (HTTP 584/InternalServerError)", e.getMessage());
        }
    }

    @Test(expected = ServerException.class)
    public void requestShouldThrowServerExceptionForBadResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(543)
                .setBody("<!-- this is not json -->"));
        new Client(key).request(Client.Method.POST, "/shrink", new byte[] {});
    }

    @Test
    public void requestShouldHaveMessageForBadResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(543)
                .setBody("<!-- this is not json -->"));
        try {
            new Client(key).request(Client.Method.POST, "/shrink", new byte[] {});
            fail("Expected an Exception to be thrown");
        } catch (Exception e) {
            assertEquals("Error while parsing response: java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path $ (HTTP 543/ParseError)", e.getMessage());
        }
    }

    @Test(expected = ClientException.class)
    public void requestShouldThrowClientException() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(492)
                .setBody("{'error':'BadRequest','message':'Oops!'}"));
        new Client(key).request(Client.Method.POST, "/shrink", new byte[] {});
    }

    @Test
    public void requestShouldHaveMessageForClientException() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(492)
                .setBody("{'error':'BadRequest','message':'Oops!'}"));
        try {
            new Client(key).request(Client.Method.POST, "/shrink", new byte[] {});
            fail("Expected an Exception to be thrown");
        } catch (Exception e) {
            assertEquals("Oops! (HTTP 492/BadRequest)", e.getMessage());
        }
    }

    @Test(expected = AccountException.class)
    public void requestShouldThrowAccountExceptionForBadCredentials() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{'error':'Unauthorized','message':'Oops!'}"));
        new Client(key).request(Client.Method.POST, "/shrink", new byte[] {});
    }

    @Test
    public void requestShouldHaveMessageForBadCredentials() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{'error':'Unauthorized','message':'Oops!'}"));
        try {
            new Client(key).request(Client.Method.POST, "/shrink", new byte[] {});
            fail("Expected an Exception to be thrown");
        } catch (Exception e) {
            assertEquals("Oops! (HTTP 401/Unauthorized)", e.getMessage());
        }
    }
}