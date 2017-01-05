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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link GooglePlus}.
 *
 * @author Richard "Shred" Körber
 */
public class GooglePlusTest {

    private final static String GOOGLE_KEY = "AIzaSyCKSbrvQasunBoV16zDH9R33D88CeLr9gQ";
    private static final String TEST_URL = "http://www.heise.de";
    private static final int LIKE_COUNT = 273558;

    private GooglePlus target;
    private HttpURLConnection connection;
    private ByteArrayOutputStream output;

    @Before
    public void setup() {
        output = new ByteArrayOutputStream();
        target = new GooglePlus() {
            @Override
            protected HttpURLConnection openConnection(URL url) throws IOException {
                assertThat(url.toExternalForm(), is("https://clients6.google.com/rpc?key=" + GOOGLE_KEY));
                connection = mock(HttpURLConnection.class);
                when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
                when(connection.getInputStream()).thenReturn(GooglePlusTest.class.getResourceAsStream("/googleplus-result.json"));
                when(connection.getOutputStream()).thenReturn(output);
                return connection;
            }
        };
    }

    @Test
    public void nameTest() {
        assertThat(new GooglePlus().getName(), is("googleplus"));
    }

    @Test
    public void counterTest() throws IOException {
        assertThat(target.count(TEST_URL), is(LIKE_COUNT));

        verify(connection).setRequestMethod("POST");
        verify(connection).setRequestProperty("Content-Type", "application/json; charset=utf-8");
        verify(connection).setDoOutput(true);

        JSONObject json = (JSONObject) new JSONTokener(output.toString()).nextValue();
        assertThat(json.getString("method"), is("pos.plusones.get"));
        assertThat(json.getString("id"), is("p"));
        assertThat(json.getString("jsonrpc"), is("2.0"));
        assertThat(json.getString("key"), is("p"));
        assertThat(json.getString("apiVersion"), is("v1"));

        JSONObject params = json.getJSONObject("params");
        assertThat(params.getString("nolog"), is("true"));
        assertThat(params.getString("id"), is(TEST_URL));
        assertThat(params.getString("source"), is("widget"));
        assertThat(params.getString("userId"), is("@viewer"));
        assertThat(params.getString("groupId"), is("@self"));
    }

}
