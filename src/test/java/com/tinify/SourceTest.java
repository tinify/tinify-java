package com.tinify;

import com.google.gson.Gson;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import mockit.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

public class SourceTest {
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

    @Test(expected = AccountException.class)
    public void withInvalidApiKeyFromFileShouldThrowAccountException() throws Exception, IOException, URISyntaxException {
        Tinify.setKey("invalid");

        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{'error':'Unauthorized','message':'Credentials are invalid'}"));

        String filePath = Paths.get(getClass().getResource("/dummy.png").toURI()).toAbsolutePath().toString();
        Source.fromFile(filePath);
    }

    @Test(expected = AccountException.class)
    public void withInvalidApiKeyFromBufferShouldThrowAccountException() throws Exception, IOException {
        Tinify.setKey("invalid");

        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{'error':'Unauthorized','message':'Credentials are invalid'}"));

        Source.fromBuffer("png file".getBytes());
    }

    @Test(expected = AccountException.class)
    public void withInvalidApiKeyFromUrlShouldThrowAccountException() throws Exception, IOException {
        Tinify.setKey("invalid");

        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{'error':'Unauthorized','message':'Credentials are invalid'}"));

        Source.fromUrl("http://example.com/test.jpg");
    }

    @Test
    public void withValidApiKeyFromFileShouldReturnSource() throws IOException, Exception, URISyntaxException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location")
                .addHeader("Compression-Count", 12));

        String filePath = Paths.get(getClass().getResource("/dummy.png").toURI()).toAbsolutePath().toString();
        assertThat(Source.fromFile(filePath), isA(Source.class));
    }

    @Test
    public void withValidApiKeyFromFileShouldReturnSourceWithData() throws IOException, Exception, URISyntaxException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("compressed file"));

        String filePath = Paths.get(getClass().getResource("/dummy.png").toURI()).toAbsolutePath().toString();
        assertThat(Source.fromFile(filePath).toBuffer(), is(equalTo("compressed file".getBytes())));
    }

    @Test
    public void withValidApiKeyFromBufferShouldReturnSource() throws IOException, Exception {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location")
                .addHeader("Compression-Count", 12));

        assertThat(Source.fromBuffer("png file".getBytes()), isA(Source.class));
    }

    @Test
    public void withValidApiKeyFromBufferShouldReturnSourceWithData() throws IOException, Exception {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("compressed file"));

        assertThat(Source.fromBuffer("png file".getBytes()).toBuffer(),
                is(equalTo("compressed file".getBytes())));
    }

    @Test
    public void withValidApiKeyFromUrlShouldReturnSource() throws IOException, Exception {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location")
                .addHeader("Compression-Count", 12));

        assertThat(Source.fromUrl("http://example.com/test.jpg"), isA(Source.class));
    }

    @Test
    public void withValidApiKeyFromUrlShouldReturnSourceWithData() throws IOException, Exception {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("compressed file"));

        assertThat(Source.fromUrl("http://example.com/test.jpg").toBuffer(),
                is(equalTo("compressed file".getBytes())));
    }

    @Test(expected = ClientException.class)
    public void withValidApiKeyFromUrlShouldThrowExceptionIfRequestIsNotOK() throws IOException, Exception {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{'error':'Source not found','message':'Cannot parse URL'}"));

        Source.fromUrl("file://wrong");
    }


    @Test
    public void withValidApiKeyResultShouldReturnResult() throws Exception, IOException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location")
                .addHeader("Compression-Count", 12));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("compressed file"));

        assertThat(Source.fromBuffer("png file".getBytes()).result(),
                isA(Result.class));
    }

    @Test
    public void withValidApiKeyResizeShouldReturnSource() throws Exception {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("small file"));

        Options options = new Options().with("width", 100).with("height", 60);

        assertThat(Source.fromBuffer("png file".getBytes()).resize(options),
                isA(Source.class));
    }

    @Test
    public void withValidApiKeyResizeShouldReturnSourceWithData() throws Exception, IOException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("small file"));

        Options options = new Options().with("width", 100).with("height", 60);

        assertThat(Source.fromBuffer("png file".getBytes()).resize(options).toBuffer(),
                is(equalTo("small file".getBytes())));
    }

    @Test
    public void withValidApiKeyStoreShouldReturnResultMeta() throws Exception {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location")
                .addHeader("Compression-Count", 12));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Location", "https://bucket.s3.amazonaws.com/example"));

        Options options = new Options().with("service", "s3");

        assertThat(Source.fromBuffer("png file".getBytes()).store(options),
                isA(ResultMeta.class));
    }

    @Test
    public void withValidApiKeyStoreShouldReturnResultMetaWithLocation() throws Exception {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location")
                .addHeader("Compression-Count", 12));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Location", "https://bucket.s3.amazonaws.com/example"));

        Options options = new Options().with("service", "s3");

        assertEquals("https://bucket.s3.amazonaws.com/example",
                Source.fromBuffer("png file".getBytes()).store(options).location());
    }

    @Test
    public void withValidApiKeyStoreShouldIncludeResizeOptionsIfSet() throws Exception, IOException, InterruptedException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location")
                .addHeader("Compression-Count", 12));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("compressed file"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Location", "https://bucket.s3.amazonaws.com/example"));

        Options resizeOptions = new Options().with("width", 100);
        Options storeOptions =  new Options().with("service", "s3");

        Source.fromBuffer("png file".getBytes()).resize(resizeOptions).store(storeOptions);

        server.takeRequest(5, TimeUnit.SECONDS);
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);

        Gson gson = new Gson();
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> body = gson.fromJson(request.getBody().readUtf8(), Map.class);

        Set<String> expectedSet = new HashSet<>();
        expectedSet.add("resize");
        expectedSet.add("store");
        assertThat(body.keySet(), everyItem(isIn(expectedSet)));
    }

    @Test
    public void withValidApiKeyToBufferShouldReturnImageData() throws Exception, IOException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location")
                .addHeader("Compression-Count", 12));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("compressed file"));

        assertThat(Source.fromBuffer("png file".getBytes()).toBuffer(),
                is(equalTo("compressed file".getBytes())));
    }

    @Test
    public void withValidApiKeyToFileShouldStoreImageData() throws Exception, IOException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location")
                .addHeader("Compression-Count", 12));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("compressed file"));

        Path tempFile = Files.createTempFile("tinify_", null);
        tempFile.toFile().deleteOnExit();

        Source.fromBuffer("png file".getBytes()).toFile(tempFile.toString());

        assertThat(Files.readAllBytes(tempFile),
                is(equalTo("compressed file".getBytes())));
    }
}
