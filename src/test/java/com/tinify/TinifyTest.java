package com.tinify;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TinifyTest {
    MockWebServer server;

    @Before
    public void setup() throws IOException {
        Logger.getLogger(MockWebServer.class.getName()).setLevel(Level.WARNING);

        server = new MockWebServer();
        server.start();
        new MockUp<HttpUrl>()
        {
            @Mock
            @SuppressWarnings("unused")
            HttpUrl parse(String url)
            {
                return new HttpUrl.Builder()
                        .scheme("http")
                        .host(server.getHostName())
                        .port(server.getPort())
                        .encodedPath(url.replaceFirst(Client.API_ENDPOINT, ""))
                        .build();
            }
        };
    }

    @After
    public void tearDown() throws IOException {
        Tinify.setKey(null);
        server.shutdown();
    }

    @Test
    public void keyShouldResetClientWithNewKey() throws Exception, InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(200));

        Tinify.setKey("abcde");
        Tinify.client();
        Tinify.setKey("fghij");
        Tinify.client().request(Client.Method.GET, "/");

        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);

        String credentials = new String(Base64.encodeBase64(("api:fghij").getBytes()));
        assertEquals("Basic " + credentials, request.getHeader("Authorization"));
    }

    @Test
    public void appIdentifierShouldResetClientWithNewAppIdentifier() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(200));

        Tinify.setKey("abcde");
        Tinify.setAppIdentifier("MyApp/1.0");
        Tinify.client();
        Tinify.setAppIdentifier("MyApp/2.0");
        Tinify.client().request(Client.Method.GET, "/");

        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals(Client.USER_AGENT + " MyApp/2.0", request.getHeader("User-Agent"));
    }

    @Test
    public void clientWithKeyShouldReturnClient() {
        Tinify.setKey("abcde");
        assertThat(Tinify.client(), isA(Client.class));
    }

    @Test(expected = AccountException.class)
    public void clientWithoutKeyShouldThrowException() {
        Tinify.client();
    }

    @Test
    public void validateWithValidKeyShouldReturnTrue() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{'error':'InputMissing','message':'No input'}"));

        Tinify.setKey("valid");
        assertThat(Tinify.validate(), is(true));

        RecordedRequest request = server.takeRequest();
        assertEquals("POST /shrink HTTP/1.1", request.getRequestLine());
        assertEquals(0, request.getBody().size());
    }

    @Test(expected = AccountException.class)
    public void validateWithErrorShouldThrowException() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{'error':'Unauthorized','message':'Credentials are invalid'}"));

        Tinify.setKey("invalid");
        Tinify.validate();

        RecordedRequest request = server.takeRequest();
        assertEquals("POST /shrink HTTP/1.1", request.getRequestLine());
        assertEquals(0, request.getBody().size());
    }

    @Test
    public void fromBufferShouldReturnSource() {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location")
                .addHeader("Compression-Count", 12));

        assertThat(Tinify.fromBuffer("png file".getBytes()), isA(Source.class));
    }

    @Test
    public void fromFileShouldReturnSource() throws IOException, URISyntaxException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location")
                .addHeader("Compression-Count", 12));

        String filePath = Paths.get(getClass().getResource("/dummy.png").toURI()).toAbsolutePath().toString();
        assertThat(Tinify.fromFile(filePath), isA(Source.class));
    }
}
