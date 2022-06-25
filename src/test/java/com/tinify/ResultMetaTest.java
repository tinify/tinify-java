package com.tinify;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import okhttp3.Headers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResultMetaTest {
    ResultMeta subject;

    @Before
    public void setup() {
        HashMap<String, String> meta = new HashMap<>();
        meta.put("Image-Width", "100");
        meta.put("Image-Height", "60");
        meta.put("Location", "https://example.com/image.png");

        subject = new ResultMeta(Headers.of(meta));
    }

    @Test
    public void withMetadataWidthShouldReturnImageWidth() {
        assertThat(subject.width(), is(equalTo(100)));
    }

    @Test
    public void withMetadataHeightShouldReturnImageHeight() {
        assertThat(subject.height(), is(equalTo(60)));
    }

    @Test
    public void withMetadataLocationShouldReturnLocation() {
        assertThat(subject.location(), is(equalTo("https://example.com/image.png")));
    }

    @Test
    public void withoutMetadataWidthShouldReturnNull() {
        subject = new ResultMeta(Headers.of());
        assertThat(subject.width(), is(nullValue()));
    }

    @Test
    public void withoutMetadataHeightShouldReturnNull() {
        subject = new ResultMeta(Headers.of());
        assertThat(subject.height(), is(nullValue()));
    }

    @Test
    public void withoutMetadataLocationShouldReturnNull() {
        subject = new ResultMeta(Headers.of());
        assertThat(subject.location(), is(nullValue()));
    }
}
