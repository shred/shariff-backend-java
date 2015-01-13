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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Flattr}.
 *
 * @author Richard "Shred" Körber
 */
public class FlattrTest {

    private static final String TEST_URL = "http://flattr4j.shredzone.org";
    private static final int LIKE_COUNT = 105;

    private Flattr target;

    @Before
    public void setup() {
        target = new Flattr() {
            @Override
            protected HttpURLConnection openConnection(URL url) throws IOException {
                assertThat(url.toExternalForm(), is("https://api.flattr.com/rest/v2/things/lookup/?url="
                            + URLEncoder.encode(TEST_URL, "utf-8")));
                HttpURLConnection connection = mock(HttpURLConnection.class);
                when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
                when(connection.getInputStream()).thenReturn(FlattrTest.class.getResourceAsStream("/flattr-result.json"));
                when(connection.getOutputStream()).thenThrow(new IllegalStateException());
                return connection;
            }
        };
    }

    @Test
    public void nameTest() {
        assertThat(target.getName(), is("flattr"));
    }

    @Test
    public void counterTest() throws IOException {
        assertThat(target.count(TEST_URL), is(LIKE_COUNT));
    }

}
