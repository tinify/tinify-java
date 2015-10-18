package com.tinify;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

public class Integration {
    private static Source optimized;

    @BeforeClass
    public static void setup() throws java.io.IOException, URISyntaxException {
        String key = System.getenv().get("TINIFY_KEY");
        if (key == null) {
            System.out.println("Set the TINIFY_KEY environment variable.");
            System.exit(1);
        }

        Tinify.setKey(key);

        String unoptimizedPath = Paths.get(Integration.class.getResource("/voormedia.png").toURI()).toAbsolutePath().toString();
        optimized = Tinify.fromFile(unoptimizedPath);
    }

    @Test
    public void shouldCompress() throws java.lang.Exception {
        Path tempFile = Files.createTempFile("tinify_", null);
        tempFile.toFile().deleteOnExit();

        Result result = optimized.result();
        result.toFile(tempFile.toString());

        assertThat(result.width(), is(equalTo(137)));
        assertThat(result.height(), is(equalTo(21)));

        long size = new File(tempFile.toString()).length();
        assertThat(size, greaterThan((long) 0));
        assertThat(size, lessThan((long) 1500));
    }

    @Test
    public void shouldResize() throws java.lang.Exception {
        Path tempFile = Files.createTempFile("tinify_", null);
        tempFile.toFile().deleteOnExit();

        Options options = new Options()
            .with("method", "fit")
            .with("width", 50)
            .with("height", 20);
        Result result = optimized.resize(options).result();
        result.toFile(tempFile.toString());

        assertThat(result.width(), is(equalTo(50)));
        assertThat(result.height(), is(equalTo(8)));

        long size = new File(tempFile.toString()).length();
        assertThat(size, greaterThan((long) 0));
        assertThat(size, lessThan((long) 800));
    }
}
