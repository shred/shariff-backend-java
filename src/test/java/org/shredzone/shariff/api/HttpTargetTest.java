/*
 * shariff-backend-java
 *
 * Copyright (C) 2017 Richard "Shred" Körber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.shariff.api;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link HttpTarget}.
 */
public class HttpTargetTest {
    private static final String TEST_NAME = "foo";
    private static final String TEST_URL = "http://example.org/shareme.html?foo=1&bar=2";
    private static final int TEST_COUNT = 1324;

    private InputStream mockIn;
    private HttpURLConnection mockConnection;

    @Before
    public void setup() throws IOException {
        mockIn = mock(InputStream.class);
        mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getInputStream()).thenReturn(mockIn);
    }

    @Test
    public void connectTest() throws IOException {
        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        TestTarget target = new TestTarget();

        assertThat(target.getName(), is(TEST_NAME));
        int count = target.count(TEST_URL);
        assertThat(count, is(TEST_COUNT));

        verify(mockConnection).getResponseCode();
        verify(mockConnection).getInputStream();
        verify(mockConnection).disconnect();
    }

    @Test(expected = IOException.class)
    public void failureTest() throws IOException {
        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_INTERNAL_ERROR);

        TestTarget target = new TestTarget();
        target.count(TEST_URL);
    }

    @TargetName(TEST_NAME)
    @TargetUrl("http://example.com/api/count?q={}")
    public class TestTarget extends HttpTarget {
        @Override
        protected HttpURLConnection openConnection(URL url) throws IOException {
            assertThat(url.toExternalForm(),
                    is("http://example.com/api/count?"
                        + "q=http%3A%2F%2Fexample.org%2Fshareme.html%3Ffoo%3D1%26bar%3D2"));
            return mockConnection;
        }

        @Override
        protected int extractCount(InputStream in) throws IOException {
            assertThat(in, sameInstance(mockIn));
            return TEST_COUNT;
        }
    }

}
