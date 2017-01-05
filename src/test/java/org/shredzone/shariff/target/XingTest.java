/*
 * shariff-backend-java
 *
 * Copyright (C) 2015 Richard "Shred" Körber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.shariff.target;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Xing}.
 *
 * @author Richard "Shred" Körber
 */
public class XingTest {

    private static final String TEST_URL = "http://www.heise.de";
    private static final int LIKE_COUNT = 123;

    private Xing target;
    private HttpURLConnection connection;
    private ByteArrayOutputStream output;

    @Before
    public void setup() {
        output = new ByteArrayOutputStream();
        target = new Xing() {
            @Override
            protected HttpURLConnection openConnection(URL url) throws IOException {
                assertThat(url.toExternalForm(), is("https://www.xing-share.com/spi/shares/statistics"));

                String result = "{\"share_counter\":\"" + LIKE_COUNT + "\"}";

                connection = mock(HttpURLConnection.class);
                when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
                when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(result.getBytes("utf-8")));
                when(connection.getOutputStream()).thenReturn(output);
                return connection;
            }
        };
    }

    @Test
    public void nameTest() {
        assertThat(new Xing().getName(), is("xing"));
    }

    @Test
    public void counterTest() throws IOException {
        assertThat(target.count(TEST_URL), is(LIKE_COUNT));

        verify(connection).setRequestMethod("POST");
        verify(connection).setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        verify(connection).setDoOutput(true);
        assertThat(output.toString(), is("url=" + URLEncoder.encode(TEST_URL, "utf-8")));
    }

}
