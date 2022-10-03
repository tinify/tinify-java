package com.tinify;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

public class Integration {
    private static Source optimized;

    @BeforeClass
    public static void setup() throws java.io.IOException, URISyntaxException {
        String key = System.getenv().get("TINIFY_KEY");
        String proxy = System.getenv().get("TINIFY_PROXY");
        if (key == null) {
            System.out.println("Set the TINIFY_KEY environment variable.");
            System.exit(1);
        }

        Tinify.setKey(key);
        Tinify.setProxy(proxy);

        String unoptimizedPath = Paths.get(Integration.class.getResource("/voormedia.png").toURI()).toAbsolutePath().toString();
        optimized = Tinify.fromFile(unoptimizedPath);
    }

    @Test
    public void shouldCompressFromFile() throws java.lang.Exception {
        Path tempFile = Files.createTempFile("tinify_", null);
        tempFile.toFile().deleteOnExit();

        Result result = optimized.result();
        result.toFile(tempFile.toString());

        long size = new File(tempFile.toString()).length();
        String contents = new String(Files.readAllBytes(Paths.get(tempFile.toString())));

        assertThat(result.width(), is(equalTo(137)));
        assertThat(result.height(), is(equalTo(21)));

        assertThat(size, greaterThan((long) 1000));
        assertThat(size, lessThan((long) 1500));

        /* width == 137 */
        assertThat(contents, containsString(new String(new byte[] {0, 0, 0, (byte)0x89})));
        assertThat(contents, not(containsString(("Copyright Voormedia"))));
    }

    @Test
    public void shouldCompressFromUrl() throws java.lang.Exception {
        Path tempFile = Files.createTempFile("tinify_", null);
        tempFile.toFile().deleteOnExit();

        optimized = Tinify.fromUrl("https://raw.githubusercontent.com/tinify/tinify-java/master/src/test/resources/voormedia.png");

        Result result = optimized.result();
        result.toFile(tempFile.toString());

        long size = new File(tempFile.toString()).length();
        String contents = new String(Files.readAllBytes(Paths.get(tempFile.toString())));

        assertThat(result.width(), is(equalTo(137)));
        assertThat(result.height(), is(equalTo(21)));

        assertThat(size, greaterThan((long) 1000));
        assertThat(size, lessThan((long) 1500));

        /* width == 137 */
        assertThat(contents, containsString(new String(new byte[] {0, 0, 0, (byte)0x89})));
        assertThat(contents, not(containsString(("Copyright Voormedia"))));
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

        long size = new File(tempFile.toString()).length();
        String contents = new String(Files.readAllBytes(Paths.get(tempFile.toString())));

        assertThat(result.width(), is(equalTo(50)));
        assertThat(result.height(), is(equalTo(8)));

        assertThat(size, greaterThan((long) 500));
        assertThat(size, lessThan((long) 1000));

        /* width == 50 */
        assertThat(contents, containsString(new String(new byte[] {0, 0, 0, (byte)0x32})));
        assertThat(contents, not(containsString(("Copyright Voormedia"))));
    }

    @Test
    public void shouldPreserveMetadata() throws java.lang.Exception {
        Path tempFile = Files.createTempFile("tinify_", null);
        tempFile.toFile().deleteOnExit();

        Result result = optimized.preserve("copyright", "creation").result();
        result.toFile(tempFile.toString());

        long size = new File(tempFile.toString()).length();
        String contents = new String(Files.readAllBytes(Paths.get(tempFile.toString())));

        assertThat(result.width(), is(equalTo(137)));
        assertThat(result.height(), is(equalTo(21)));

        assertThat(size, greaterThan((long) 1000));
        assertThat(size, lessThan((long) 2000));

        /* width == 137 */
        assertThat(contents, containsString(new String(new byte[] {0, 0, 0, (byte)0x89})));
        assertThat(contents, containsString(("Copyright Voormedia")));
    }

    @Test
    public void shouldConvertFile() throws java.lang.Exception {
        Path tempFile = Files.createTempFile("tinify_", null);
        tempFile.toFile().deleteOnExit();

        Result result = optimized.convert(new Options().with("type", "image/webp")).result();
        result.toFile(tempFile.toString());

        long size = new File(tempFile.toString()).length();

        assertThat(result.width(), is(equalTo(137)));
        assertThat(result.height(), is(equalTo(21)));
        assertThat(result.mediaType(), is(equalTo("image/webp")));

        assertThat(size, greaterThan((long) 1000));
        assertThat(size, lessThan((long) 2000));
    }

    @Test
    public void shouldTransformFile() throws java.lang.Exception {
        Path tempFile = Files.createTempFile("tinify_", null);
        tempFile.toFile().deleteOnExit();
        Result result = optimized.transform(new Options().with("background", "black")).result();
        result.toFile(tempFile.toString());

        long size = new File(tempFile.toString()).length();
        String contents = new String(Files.readAllBytes(Paths.get(tempFile.toString())));

        assertThat(result.width(), is(equalTo(137)));
        assertThat(result.height(), is(equalTo(21)));

        assertThat(size, greaterThan((long) 1000));
        assertThat(size, lessThan((long) 2000));

        /* width == 137 */
        assertThat(contents, containsString(new String(new byte[] {0, 0, 0, (byte)0x89})));
    }
}
