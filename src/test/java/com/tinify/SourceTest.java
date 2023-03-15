package com.tinify;

import com.google.gson.Gson;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;

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

    public void assertJsonEquals(String expected, String actual)
    {
        Gson gson = new Gson();
        @SuppressWarnings("unchecked")
        Map<String, Object> expectedMap = gson.fromJson(expected, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> actualMap = gson.fromJson(actual, Map.class);

        assertEquals(expectedMap, actualMap);
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
    public void withValidApiKeyFromUrlShouldReturnSourceWithData() throws IOException, Exception, InterruptedException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("compressed file"));

        assertThat(Source.fromUrl("http://example.com/test.jpg").toBuffer(),
                is(equalTo("compressed file".getBytes())));

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertJsonEquals("{\"source\":{\"url\":\"http://example.com/test.jpg\"}}", request1.getBody().readUtf8());
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
    public void withValidApiKeyPreserveShouldReturnSource() throws Exception, InterruptedException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("copyrighted file"));

        assertThat(Source.fromBuffer("png file".getBytes()).preserve("copyright", "location"),
               isA(Source.class));

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertEquals("png file", request1.getBody().readUtf8());
    }

    @Test
    public void withValidApiKeyConvertShouldReturnSource() throws Exception, InterruptedException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("copyrighted file"));

        assertThat(Source.fromBuffer("png file".getBytes()).convert(new Options().with("type", "image/webp")),
               isA(Source.class));

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertEquals("png file", request1.getBody().readUtf8());
    }

    @Test
    public void withValidApiKeyPreserveShouldReturnSourceWithData() throws Exception, IOException, InterruptedException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("copyrighted file"));

        assertThat(Source.fromBuffer("png file".getBytes()).preserve("copyright", "location").toBuffer(),
                is(equalTo("copyrighted file".getBytes())));

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertEquals("png file", request1.getBody().readUtf8());

        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertJsonEquals("{\"preserve\":[\"copyright\",\"location\"]}", request2.getBody().readUtf8());
    }

    @Test
    public void withValidApiKeyPreserveShouldReturnSourceWithDataForArray() throws Exception, IOException, InterruptedException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("copyrighted file"));

        String[] options = new String [] {"copyright", "location"};
        assertThat(Source.fromBuffer("png file".getBytes()).preserve(options).toBuffer(),
                is(equalTo("copyrighted file".getBytes())));

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertEquals("png file", request1.getBody().readUtf8());

        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertJsonEquals("{\"preserve\":[\"copyright\",\"location\"]}", request2.getBody().readUtf8());
    }

    @Test
    public void withValidApiKeyPreserveShouldIncludeOtherOptionsIfSet() throws Exception, IOException, InterruptedException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("copyrighted resized file"));

        Options resizeOptions = new Options().with("width", 100).with("height", 60);
        String[] preserveOptions = new String [] {"copyright", "location"};
        assertThat(Source.fromBuffer("png file".getBytes()).resize(resizeOptions).preserve(preserveOptions).toBuffer(),
                is(equalTo("copyrighted resized file".getBytes())));

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertEquals("png file", request1.getBody().readUtf8());

        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertJsonEquals("{\"resize\":{\"width\":100,\"height\":60},\"preserve\":[\"copyright\",\"location\"]}", request2.getBody().readUtf8());
    }

    @Test
    public void withValidApiKeyResizeShouldReturnSource() throws Exception, InterruptedException {
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

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertEquals("png file", request1.getBody().readUtf8());
    }

    @Test
    public void withValidApiKeyResizeShouldReturnSourceWithData() throws Exception, IOException, InterruptedException {
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

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertEquals("png file", request1.getBody().readUtf8());

        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertJsonEquals("{\"resize\":{\"width\":100,\"height\":60}}", request2.getBody().readUtf8());
    }

    @Test
    public void withValidApiKeyTransformShouldReturnSourceWithData() throws Exception, IOException, InterruptedException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Location", "https://api.tinify.com/some/location"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("small file"));

        Options options = new Options().with("background", "black");

        assertThat(Source.fromBuffer("png file".getBytes()).transform(options).toBuffer(),
                is(equalTo("small file".getBytes())));

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertEquals("png file", request1.getBody().readUtf8());

        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertJsonEquals("{\"transform\":{\"background\":\"black\"}}", request2.getBody().readUtf8());
    }

    @Test
    public void withValidApiKeyStoreShouldReturnResultMeta() throws Exception, InterruptedException {
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

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertEquals("png file", request1.getBody().readUtf8());

        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertJsonEquals("{\"store\":{\"service\":\"s3\"}}", request2.getBody().readUtf8());
    }

    @Test
    public void withValidApiKeyStoreShouldReturnResultMetaWithLocation() throws Exception, InterruptedException {
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

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertEquals("png file", request1.getBody().readUtf8());

        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertJsonEquals("{\"store\":{\"service\":\"s3\"}}", request2.getBody().readUtf8());
    }

    @Test
    public void withValidApiKeyStoreShouldIncludeOtherOptionsIfSet() throws Exception, IOException, InterruptedException {
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

        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertEquals("png file", request1.getBody().readUtf8());

        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertJsonEquals("{\"resize\":{\"width\":100},\"store\":{\"service\":\"s3\"}}", request2.getBody().readUtf8());
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


    /*
     * The following tests should probably be done with parametrized tests
     */
    @Test
    public void withOptionsNotEmptyResultDoesAPOST() throws Exception, IOException, InterruptedException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("compressed file"));
        Result result = new Source("https://api.tinify.com/some/location", new Options().with("I am not", "empty")).result();
        RecordedRequest outputRequest = server.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("POST", outputRequest.getMethod());
    }

    @Test
    public void withOptionsNULLResultDoesAGET() throws Exception, IOException, InterruptedException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("compressed file"));
        Result result = new Source("https://api.tinify.com/some/location", null).result();
        RecordedRequest outputRequest = server.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("GET", outputRequest.getMethod());
    }

   @Test
    public void withOptionsEmptyResultDoesAGET() throws Exception, IOException, InterruptedException {
        Tinify.setKey("valid");

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("compressed file"));
        Result result = new Source("https://api.tinify.com/some/location", new Options()).result();
        RecordedRequest outputRequest = server.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("GET", outputRequest.getMethod());
    }
}