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

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;

public class ClientErrorTest {
    Client subject;
    MockWebServer server;
    String key = "key";

    @Before
    public void setup() throws IOException {
        Logger.getLogger(MockWebServer.class.getName()).setLevel(Level.WARNING);
        server = new MockWebServer();
        subject = new Client(key);
        server.start();
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

}
