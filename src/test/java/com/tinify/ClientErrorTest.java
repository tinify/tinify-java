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
    public void requestWithTimeoutShouldThrowConnectionException() throws Exception {
        new Expectations() {{
            httpClient.newCall((Request) any); result = new java.net.SocketTimeoutException("SocketTimeoutException");
        }};
        new Client(key, null).request(Client.Method.POST, "http://shrink", new byte[] {});
    }

    @Test
    public void requestWithTimeoutShouldThrowExceptionWithMessage() throws Exception {
        new Expectations() {{
            httpClient.newCall((Request) any); result = new java.net.SocketTimeoutException("SocketTimeoutException");
        }};
        try {
            new Client(key, null).request(Client.Method.POST, "http://shrink", new byte[] {});
            fail("Expected an Exception to be thrown");
        } catch (ConnectionException e) {
            assertEquals("Error while connecting: SocketTimeoutException", e.getMessage());
        }
    }

    @Test(expected = ConnectionException.class)
    public void requestWithSocketErrorShouldThrowConnectionException() throws Exception {
        new Expectations() {{
            httpClient.newCall((Request) any); result = new java.net.UnknownHostException("UnknownHostException");
        }};
        new Client(key, null).request(Client.Method.POST, "http://shrink", new byte[] {});
    }

    @Test
    public void requestWithSocketErrorShouldThrowExceptionWithMessage() throws Exception {
        new Expectations() {{
            httpClient.newCall((Request) any); result = new java.net.UnknownHostException("UnknownHostException");
        }};
        try {
            new Client(key, null).request(Client.Method.POST, "http://shrink", new byte[] {});
            fail("Expected an Exception to be thrown");
        } catch (ConnectionException e) {
            assertEquals("Error while connecting: UnknownHostException", e.getMessage());
        }
    }

    @Test(expected = ConnectionException.class)
    public void requestWithUnexpectedExceptionShouldThrowConnectionException() throws Exception {
        new Expectations() {{
            httpClient.newCall((Request) any); result = new java.lang.Exception("Some exception");
        }};
        new Client(key, null).request(Client.Method.POST, "http://shrink", new byte[] {});
    }

    @Test
    public void requestWithUnexpectedExceptionShouldThrowExceptionWithMessage() throws Exception {
        new Expectations() {{
            httpClient.newCall((Request) any); result = new java.lang.Exception("Some exception");
        }};
        try {
            new Client(key, null).request(Client.Method.POST, "http://shrink", new byte[] {});
            fail("Expected an Exception to be thrown");
        } catch (ConnectionException e) {
            assertEquals("Error while connecting: Some exception", e.getMessage());
        }
    }
}
