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
package org.shredzone.shariff;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ShariffServlet}.
 *
 * @author Richard "Shred" Körber
 */
public class ShariffServletTest {

    private ShariffServlet servlet;

    @Before
    @SuppressWarnings("serial")
    public void setup() {
        servlet = new ShariffServlet() {
            @Override
            protected ShariffBackend createBackend() {
                Map<String, Integer> counts = new TreeMap<>();
                counts.put("facebook", 123);
                counts.put("twitter", 456);
                counts.put("googleplus", 789);

                try {
                    ShariffBackend backend = mock(ShariffBackend.class);
                    when(backend.getCounts("http://example.com/testpage")).thenReturn(counts);
                    return backend;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    @Test
    public void noUrlTest() throws ServletException, IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("url")).thenReturn(null);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "missing url parameter");
    }

    @Test
    public void invalidUrlTest() throws ServletException, IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("url")).thenReturn("#################################");
        when(req.getServerName()).thenReturn("example.com");

        HttpServletResponse resp = mock(HttpServletResponse.class);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid host");
    }

    @Test
    public void invalidHostTest() throws ServletException, IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("url")).thenReturn("http://illegal.com/testpage");
        when(req.getServerName()).thenReturn("example.com");

        HttpServletResponse resp = mock(HttpServletResponse.class);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid host");
    }

    @Test
    public void validTest() throws ServletException, IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("url")).thenReturn("http://example.com/testpage");
        when(req.getServerName()).thenReturn("example.com");

        StringWriter out = new StringWriter();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(req, resp);

        verify(resp, never()).sendError(anyInt(), anyString());
        verify(resp).setContentType("Content-type: application/json");
        verify(resp).setCharacterEncoding("utf-8");

        assertThat(out.toString(), is("{\"twitter\":456,\"facebook\":123,\"googleplus\":789}"));
    }

}
