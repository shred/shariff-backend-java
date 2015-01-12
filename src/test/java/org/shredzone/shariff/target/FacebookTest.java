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
import java.net.URL;
import java.net.URLEncoder;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Facebook}.
 *
 * @author Richard "Shred" Körber
 */
public class FacebookTest {

    private static final String TEST_URL = "http://www.heise.de";
    private static final int SHARE_COUNT = 1234;

    private Facebook target;

    @Before
    public void setup() {
        target = new Facebook() {
            @Override
            protected HttpURLConnection openConnection(URL url) throws IOException {
                assertThat(url.toExternalForm(), is("https://api.facebook.com/method/fql.query"
                            + "?format=json"
                            + "&query="
                            + URLEncoder.encode("select share_count from link_stat where url=\"" + TEST_URL + "\"", "utf-8")));

                String result = "[{\"share_count\":" + SHARE_COUNT + "}]";

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
        assertThat(target.getName(), is("facebook"));
    }

    @Test
    public void counterTest() throws IOException {
        assertThat(target.count(TEST_URL), is(SHARE_COUNT));
    }

}
