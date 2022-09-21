package com.tinify;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import okhttp3.Headers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResultTest {
    Result subject;

    @Before
    public void setup() {
        HashMap<String, String> meta = new HashMap<>();
        meta.put("Image-Width", "100");
        meta.put("Image-Height", "60");
        meta.put("Content-Length", "450");
        meta.put("Content-Type", "image/png");

        subject = new Result(Headers.of(meta), "image data".getBytes());
    }

    @Test
    public void withMetaAndDataWidthShouldReturnImageWidth() {
        assertThat(subject.width(), is(equalTo(100)));
    }

    @Test
    public void withMetaAndDataHeightShouldReturnImageHeight() {
        assertThat(subject.height(), is(equalTo(60)));
    }

    @Test
    public void withMetaAndDataLocationShouldReturnNull() {
        assertThat(subject.location(), is(nullValue()));
    }

    @Test
    public void withMetaAndDataSizeShouldReturnContentLength() {
        assertThat(subject.size(), is(equalTo(450)));
    }

    @Test
    public void withMetaAndDataMediaTypeShouldReturnContentType() {
        assertThat(subject.mediaType(), is(equalTo("image/png")));
    }

    @Test
    public void withMetaAndDataToBufferShouldReturnImageData() {
        assertThat(subject.toBuffer(), is(equalTo("image data".getBytes())));
    }

    @Test
    public void withMetaAndDataMediaTypeShouldReturnExtension() {
        assertThat(subject.extension(), is(equalTo("png")));
    }

    @Test
    public void withoutMetaAndDataWidthShouldReturnNull() {
        subject = new Result(Headers.of(), null);
        assertThat(subject.width(), is(nullValue()));
    }

    @Test
    public void withoutMetaAndDataHeightShouldReturnNull() {
        subject = new Result(Headers.of(), null);
        assertThat(subject.height(), is(nullValue()));
    }

    @Test
    public void withoutMetaAndDataLocationShouldReturnNull() {
        subject = new Result(Headers.of(), null);
        assertThat(subject.location(), is(nullValue()));
    }

    @Test
    public void withoutMetaAndDataSizeShouldReturnNull() {
        subject = new Result(Headers.of(), null);
        assertThat(subject.size(), is(nullValue()));
    }

    @Test
    public void withoutMetaAndDataContentTypeShouldReturnNull() {
        subject = new Result(Headers.of(), null);
        assertThat(subject.mediaType(), is(nullValue()));
    }

    @Test
    public void withoutMetaAndDataToBufferShouldReturnNull() {
        subject = new Result(Headers.of(), null);
        assertThat(subject.toBuffer(), is(nullValue()));
    }

    @Test
    public void withoutMetaAndDataMediaTypeShouldReturnNull() {
        subject = new Result(Headers.of(), null);
        assertThat(subject.extension(), is(nullValue()));
    }
}
