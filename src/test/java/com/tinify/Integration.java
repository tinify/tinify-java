package com.tinify;

import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

public class Integration {
    private static Source optimized;

    @BeforeClass
    public static void setup() throws java.io.IOException {
        String key = System.getenv().get("TINIFY_KEY");
        if (key == null) {
            System.out.println("Set the TINIFY_KEY environment variable.");
            System.exit(1);
        }

        Tinify.setKey(key);

        String unoptimizedPath = Integration.class.getResource("/voormedia.png").getFile();
        optimized = Tinify.fromFile(unoptimizedPath);
    }

    @Test
    public void shouldCompress() throws java.lang.Exception {
        Path tempFile = Files.createTempFile("tinify_", null);
        tempFile.toFile().deleteOnExit();

        optimized.toFile(tempFile.toString());

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
        optimized.resize(options).toFile(tempFile.toString());

        long size = new File(tempFile.toString()).length();
        assertThat(size, greaterThan((long) 0));
        assertThat(size, lessThan((long) 800));
    }
}
