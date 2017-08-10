package com.tinify;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ClientEndpointTest {
    Client subject;
    String key = "key";

    @Mocked
    OkHttpClient httpClient;

    @Before
    public void setup() throws IOException {
        subject = new Client(key);
    }

    @Test
    public void requestShouldAddApiEndpoint() throws Exception {
        new Expectations() {{
            httpClient.newCall((Request) any);
            result = new ConnectionException();
        }};

        try {
            subject.request(Client.Method.POST, "/shrink", new byte[] {});
        } catch(Exception e) {
            // not interested in result
        }
        new Verifications() {{
            Request request;
            httpClient.newCall(request = withCapture());
            assertEquals("https://api.tinify.com/shrink", request.url().toString());
        }};
    }

    @Test
    public void requestShouldNotAddApiEndpoint() throws Exception {
        final String url = "https://api.tinify.com/output/13259adadfq3.png";

        new Expectations() {{
            httpClient.newCall((Request) any);
            result = new ConnectionException();
        }};

        try {
            subject.request(Client.Method.POST, url, new byte[] {});
        } catch(Exception e) {
            // not interested in result
        }

        new Verifications() {{
            Request request;
            httpClient.newCall(request = withCapture());
            assertEquals(url, request.url().toString());
        }};
    }
}
