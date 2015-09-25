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
    public void withoutMetaAndDataWidthShouldReturnNull() {
        subject = new Result(new HashMap<String, List<String>>(), null);
        assertThat(subject.width(), is(nullValue()));
    }

    @Test
    public void withoutMetadataHeightShouldReturnNull() {
        subject = new Result(new HashMap<String, List<String>>(), null);
        assertThat(subject.height(), is(nullValue()));
    }

    @Test
    public void withoutMetadataLocationShouldReturnNull() {
        subject = new Result(new HashMap<String, List<String>>(), null);
        assertThat(subject.location(), is(nullValue()));
    }

    @Test
    public void withoutMetadataSizeShouldReturnNull() {
        subject = new Result(new HashMap<String, List<String>>(), null);
        assertThat(subject.size(), is(nullValue()));
    }

    @Test
    public void withoutMetadataContentTypeShouldReturnNull() {
        subject = new Result(new HashMap<String, List<String>>(), null);
        assertThat(subject.mediaType(), is(nullValue()));
    }

    @Test
    public void withoutMetadataToBufferShouldReturnNull() {
        subject = new Result(new HashMap<String, List<String>>(), null);
        assertThat(subject.toBuffer(), is(nullValue()));
    }
}
