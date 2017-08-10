package com.tinify;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
