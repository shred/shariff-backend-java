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
    private static final String ACCESS_TOKEN = "access_token=abcABC123";
    private static final String CLIENT_ID = "fbclient";
    private static final String CLIENT_SECRET = "sekrit";
    private static final int SHARE_COUNT = 7710;

    private Facebook target;

    @Before
    public void setup() {
        target = new Facebook() {
            @Override
            protected HttpURLConnection openConnection(URL url) throws IOException {
                assertThat(url.toExternalForm(), is("https://graph.facebook.com/v2.8/"
                            + "?id=" + URLEncoder.encode(TEST_URL, "utf-8")
                              + "&" + ACCESS_TOKEN));

                HttpURLConnection connection = mock(HttpURLConnection.class);
                when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
                when(connection.getInputStream()).thenReturn(FacebookTest.class.getResourceAsStream("/facebook-result.json"));
                when(connection.getOutputStream()).thenThrow(new IllegalStateException());
                return connection;
            }

            @Override
            protected String getAccessToken() throws IOException {
                return ACCESS_TOKEN;
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
    public void accessTokenTest() throws IOException {
        Facebook tokenTarget = new Facebook() {
            @Override
            protected HttpURLConnection openConnection(URL url) throws IOException {
                assertThat(url.toExternalForm(), is("https://graph.facebook.com/oauth/access_token"
                            + "?client_id=" + CLIENT_ID
                            + "&client_secret=" + CLIENT_SECRET
                            + "&grant_type=client_credentials"));

                HttpURLConnection connection = mock(HttpURLConnection.class);
                when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
                when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(ACCESS_TOKEN.getBytes("utf-8")));
                when(connection.getOutputStream()).thenThrow(new IllegalStateException());
                return connection;
            }
        };

        tokenTarget.setSecret(CLIENT_ID, CLIENT_SECRET);

        String token = tokenTarget.getAccessToken();
        assertThat(token, is(ACCESS_TOKEN));
    }

}
