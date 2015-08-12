package com.tinify;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class ResultTest {
    Map<String, List<String>> meta;
    Result subject;

    @Before
    public void setup() {
        meta = new HashMap<>();
        meta.put("Image-Width", new ArrayList<>(Arrays.asList("100")));
        meta.put("Image-Height", new ArrayList<>(Arrays.asList("60")));
        meta.put("Content-Length", new ArrayList<>(Arrays.asList("450")));
        meta.put("Content-Type", new ArrayList<>(Arrays.asList("image/png")));

        subject = new Result(meta, "image data".getBytes());
    }

    @Test
    public void widthShouldReturnImageWidth() {
        assertThat(subject.width(), is(equalTo(100)));
    }

    @Test
    public void heightShouldReturnImageHeight() {
        assertThat(subject.height(), is(equalTo(60)));
    }

    @Test
    public void locationShouldReturnNull() {
        assertThat(subject.location(), is(nullValue()));
    }

    @Test
    public void sizeShouldReturnContentLength() {
        assertThat(subject.size(), is(equalTo(450)));
    }

    @Test
    public void mediaTypeShouldReturnContentType() {
        assertThat(subject.mediaType(), is(equalTo("image/png")));
    }

    @Test
    public void toBufferShouldReturnImageData() {
        assertThat(subject.toBuffer(), is(equalTo("image data".getBytes())));
    }
}
