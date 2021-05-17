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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.junit.Before;
import org.junit.Test;
import org.shredzone.shariff.RateLimitExceededException;

/**
 * Unit tests for {@link Facebook}.
 *
 * @author Richard "Shred" Körber
 */
public class FacebookTest {

    private static final String TEST_URL = "http://www.heise.de";
    private static final String CLIENT_ID = "fbclient";
    private static final String CLIENT_SECRET = "sekrit";
    private static final int SHARE_COUNT = 9013;

    private Facebook target;

    @Before
    public void setup() {
        target = new Facebook() {
            @Override
            protected HttpURLConnection openConnection(URL url) throws IOException {
                assertThat(url.toExternalForm(), is("https://graph.facebook.com/v10.0"
                            + "/?id=" + URLEncoder.encode(TEST_URL, "utf-8")
                            + "&fields=engagement"
                            + "&access_token=" + CLIENT_ID + "|" + CLIENT_SECRET));

                HttpURLConnection connection = mock(HttpURLConnection.class);
                when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
                when(connection.getInputStream()).thenReturn(FacebookTest.class.getResourceAsStream("/facebook-result.json"));
                when(connection.getOutputStream()).thenThrow(new IllegalStateException());
                return connection;
            }
        };
    }

    @Test
    public void nameTest() {
        assertThat(new Facebook().getName(), is("facebook"));
    }

    @Test
    public void counterTest() throws IOException {
        target.setSecret(CLIENT_ID, CLIENT_SECRET);
        assertThat(target.count(TEST_URL), is(SHARE_COUNT));
    }

    @Test(expected = IllegalStateException.class)
    public void anonymousCounterTest() throws IOException {
        target.count(TEST_URL);
    }

    @Test
    public void rateLimitNotExceededTest() throws IOException {
        target.checkAppUsageHeader("{\"call_count\":98,\"total_time\":24,\"total_cputime\":100}");
    }

    @Test(expected = RateLimitExceededException.class)
    public void rateLimitExceededTest() throws IOException {
        target.checkAppUsageHeader("{\"call_count\":98,\"total_time\":24,\"total_cputime\":104}");
    }

    @Test
    public void badRateLimitHeaderTest() throws IOException {
        target.checkAppUsageHeader("{}");
        target.checkAppUsageHeader("[123, 45]");
        target.checkAppUsageHeader("{\"call_count\":\"foo\"}");
        target.checkAppUsageHeader("not-json");
    }

}
