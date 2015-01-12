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

import org.junit.Test;

/**
 * Unit tests for {@link JSONTarget}.
 *
 * @author Richard "Shred" Körber
 */
public class JSONTargetTest {

    private static final String TEST_URL = "http://www.heise.de";

    @Test(expected = IOException.class)
    public void badResponseTest() throws IOException {
        Twitter target = new Twitter() {
            @Override
            protected HttpURLConnection openConnection(URL url) throws IOException {
                HttpURLConnection connection = mock(HttpURLConnection.class);
                when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_INTERNAL_ERROR);
                when(connection.getResponseMessage()).thenReturn("internal server error");
                return connection;
            }
        };

        try {
            target.count(TEST_URL);
        } catch (IOException ex) {
            assertThat(ex.getMessage(), is("HTTP 500: internal server error"));
            throw ex;
        }
    }

}
