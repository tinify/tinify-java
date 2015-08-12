package com.tinify;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.*;

import static org.junit.Assert.assertEquals;

public class ClientEndpointTest {
    Client subject;
    String key = "ABC123456789";

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
            assertEquals("https://api.tinify.com/shrink", request.urlString());
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
            assertEquals(url, request.urlString());
        }};
    }
}
