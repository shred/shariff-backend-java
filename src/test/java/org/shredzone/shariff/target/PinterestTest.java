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
import java.io.IOException;
import java.net.HttpURLConnection;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Pinterest}.
 *
 * @author Richard "Shred" Körber
 */
public class PinterestTest {

    private static final String TEST_URL = "http://www.heise.de";
    private static final int LIKE_COUNT = 15089;

    private Pinterest target;

    @Before
    public void setup() {
        target = new Pinterest() {
            @Override
            protected HttpURLConnection connect(String url) throws IOException {
                assertThat(url, is(TEST_URL));
                String result = "x({\"url\":\"" + TEST_URL + "\",\"count\":" + LIKE_COUNT + "})";

                HttpURLConnection connection = mock(HttpURLConnection.class);
                when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
                when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(result.getBytes("utf-8")));
                when(connection.getOutputStream()).thenThrow(new IllegalStateException());
                return connection;
            }
        };
    }

    @Test
    public void nameTest() {
        assertThat(new Pinterest().getName(), is("pinterest"));
    }

    @Test
    public void counterTest() throws IOException {
        assertThat(target.count(TEST_URL), is(LIKE_COUNT));
    }

}
