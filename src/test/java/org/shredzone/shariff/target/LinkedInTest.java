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
 * Unit test for {@link LinkedIn}.
 *
 * @author Richard "Shred" Körber
 */
public class LinkedInTest {

    private static final String TEST_URL = "http://www.heise.de";
    private static final int LIKE_COUNT = 15089;

    private LinkedIn target;

    @Before
    public void setup() {
        target = new LinkedIn() {
            @Override
            protected HttpURLConnection connect(String url) throws IOException {
                assertThat(url, is(TEST_URL));
                String result = "IN.Tags.Share.handleCount({\"count\":" + LIKE_COUNT + ",\"fCnt\":\"121\",\"fCntPlusOne\":\"122\",\"url\":\"" + TEST_URL.replace("/", "\\/") + "\"});";

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
        assertThat(new LinkedIn().getName(), is("linkedin"));
    }

    @Test
    public void counterTest() throws IOException {
        assertThat(target.count(TEST_URL), is(LIKE_COUNT));
    }

}
