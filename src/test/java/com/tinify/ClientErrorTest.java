package com.tinify;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ClientErrorTest {
    Client subject;
    MockWebServer server;
    String key = "ABC123456789";

    @Mocked
    OkHttpClient httpClient;

    @Before
    public void setup() throws IOException {
        Logger.getLogger(MockWebServer.class.getName()).setLevel(Level.WARNING);
        server = new MockWebServer();
        subject = new Client(key, null);
        server.start();
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test(expected = ConnectionException.class)
    public void shouldThrowConnectionError() throws Exception {
        new Expectations() {{
            httpClient.newCall((Request) any); result = new java.io.IOException(
                    new java.net.SocketTimeoutException());
        }};
        new Client(key, null).request(Client.Method.POST, "http://shrink", new byte[] {});
    }

    @Test
    public void shouldHaveMessageOnConnectionError() throws Exception {
        new Expectations() {{
            httpClient.newCall((Request) any); result = new java.io.IOException(
                    new java.net.SocketTimeoutException());
        }};
        try {
            new Client(key, null).request(Client.Method.POST, "http://shrink", new byte[] {});
            fail("Expected an Exception to be thrown");
        } catch (Exception e) {
            assertEquals("Error while connecting: java.net.SocketTimeoutException", e.getMessage());
        }
    }
}
