package com.tinify;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class FormatTest {
    @Test
    public void formatShouldExposeSupportedMediaTypes() {
        assertThat(Format.WEBP, is(equalTo("image/webp")));
        assertThat(Format.PNG, is(equalTo("image/png")));
        assertThat(Format.JPEG, is(equalTo("image/jpeg")));
        assertThat(Format.JPG, is(equalTo("image/jpg")));
        assertThat(Format.AVIF, is(equalTo("image/avif")));
        assertThat(Format.JXL, is(equalTo("image/jxl")));
        assertThat(Format.ANY, is(equalTo("*/*")));
    }

    @Test
    public void convertWithFormatShouldSerializeMediaType() {
        Options options = new Options().with("convert", new Options().with("type", Format.JXL));
        assertThat(options.toJson(), is(equalTo("{\"convert\":{\"type\":\"image/jxl\"}}")));
    }

    @Test
    public void convertWithMultipleFormatsShouldSerializeMediaTypes() {
        Options options = new Options().with("convert",
                new Options().with("type", new String[]{Format.JXL, Format.WEBP}));
        assertThat(options.toJson(),
                is(equalTo("{\"convert\":{\"type\":[\"image/jxl\",\"image/webp\"]}}")));
    }
}
