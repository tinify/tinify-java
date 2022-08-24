package com.tinify;

import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

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
            HttpUrl parse(Invocation inv, String url)
            {
                if (url.contains("localhost")) {
                    return inv.proceed();
                } else {
                    return new HttpUrl.Builder()
                            .scheme("http")
                            .host(server.getHostName())
                            .port(server.getPort())
                            .encodedPath(url.replaceFirst(".*(/.*)", "$1"))
                            .build();
                }
            }
        };
    }

    @After
    public void tearDown() throws IOException {
        Tinify.setKey(null);
        Tinify.setProxy(null);
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
    public void proxyShouldResetClientWithNewProxy() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(407));
        server.enqueue(new MockResponse().setResponseCode(200));

        Tinify.setKey("abcde");
        Tinify.setProxy("http://localhost");
        Tinify.client();
        Tinify.setProxy("http://user:pass@" + server.getHostName() + ":" + server.getPort());
        Tinify.client().request(Client.Method.GET, "/");

        RecordedRequest request1 = server.takeRequest(5, TimeUnit.SECONDS);
        RecordedRequest request2 = server.takeRequest(5, TimeUnit.SECONDS);
        assertEquals("Basic dXNlcjpwYXNz", request2.getHeader("Proxy-Authorization"));
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

    @Test(expected = ConnectionException.class)
    public void clientWithInvalidProxyShouldThrowException() {
        Tinify.setKey("abcde");
        Tinify.setProxy("http-bad-url");
        Tinify.client();
    }

    @Test
    public void validateWithValidKeyShouldReturnTrue() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{'error':'Input missing','message':'No input'}"));

        Tinify.setKey("valid");
        assertThat(Tinify.validate(), is(true));

        RecordedRequest request = server.takeRequest();
        assertEquals("POST /shrink HTTP/1.1", request.getRequestLine());
        assertEquals(0, request.getBody().size());
    }

    @Test
    public void validateWithLimitedKeyShouldReturnTrue() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("{'error':'Too many requests','message':'Your monthly limit has been exceeded'}"));

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
