package com.tinify;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class ResultMetaTest {

    ResultMeta subject;

    @Before
    public void setup() {
        Map<String, List<String>> meta = new HashMap<>();
        meta.put("Image-Width", new ArrayList<>(Arrays.asList("100")));
        meta.put("Image-Height", new ArrayList<>(Arrays.asList("60")));
        meta.put("Location", new ArrayList<>(Arrays.asList("https://example.com/image.png")));

        subject = new ResultMeta(meta);
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
        Map<String, List<String>> meta = new HashMap<>();
        subject = new ResultMeta(meta);
        assertThat(subject.width(), is(nullValue()));
    }

    @Test
    public void withoutMetadataHeightShouldReturnNull() {
        Map<String, List<String>> meta = new HashMap<>();
        subject = new ResultMeta(meta);
        assertThat(subject.height(), is(nullValue()));
    }

    @Test
    public void withoutMetadataLocationShouldReturnNull() {
        Map<String, List<String>> meta = new HashMap<>();
        subject = new ResultMeta(meta);
        assertThat(subject.location(), is(nullValue()));
    }
}
