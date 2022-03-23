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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
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
                counts.put("reddit", 456);

                ShariffBackend backend = mock(ShariffBackend.class);
                when(backend.getCounts("http://example.com/testpage")).thenReturn(counts);
                return backend;
            }
        };
    }

    @Test
    public void noUrlTest() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("url")).thenReturn(null);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "missing url parameter");
    }

    @Test
    public void invalidUrlTest() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("url")).thenReturn("#################################");
        when(req.getServerName()).thenReturn("example.com");

        HttpServletResponse resp = mock(HttpServletResponse.class);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid host");
    }

    @Test
    public void invalidHostTest() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("url")).thenReturn("http://illegal.com/testpage");
        when(req.getServerName()).thenReturn("example.com");

        HttpServletResponse resp = mock(HttpServletResponse.class);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid host");
    }

    @Test
    public void validTest() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("url")).thenReturn("http://example.com/testpage");
        when(req.getServerName()).thenReturn("example.com");

        StringWriter out = new StringWriter();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(req, resp);

        verify(resp, never()).sendError(anyInt(), anyString());
        verify(resp).setContentType("application/json");
        verify(resp).setCharacterEncoding("UTF-8");

        assertThat(out.toString(), is("{\"facebook\":123,\"reddit\":456}"));
    }

    @Test
    public void initTest() throws ServletException {
        ShariffServlet realServlet = new ShariffServlet();

        ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameter("host")).thenReturn("http://example\\.com");
        when(config.getInitParameter("cache.size")).thenReturn("50");
        when(config.getInitParameter("cache.timeToLiveMs")).thenReturn("1000000");
        when(config.getInitParameter("cache.useOnError")).thenReturn("true");
        when(config.getInitParameter("targets")).thenReturn("facebook,reddit");
        when(config.getInitParameter("threads")).thenReturn("5");
        when(config.getInitParameter("facebook.id")).thenReturn("12345");
        when(config.getInitParameter("facebook.secret")).thenReturn("54321");
        when(config.getInitParameter("organisation")).thenReturn("acme corp");

        realServlet.init(config);

        assertThat(realServlet.hostPattern.pattern(), is("http://example\\.com"));
        assertThat(realServlet.cacheSize, is(50));
        assertThat(realServlet.timeToLiveMs, is(1000000L));
        assertThat(realServlet.useCacheOnError, is(true));
        assertThat(realServlet.targets, is(arrayContaining("facebook", "reddit")));
        assertThat(realServlet.threads, is(5));
        assertThat(realServlet.fbClientId, is("12345"));
        assertThat(realServlet.fbClientSecret, is("54321"));
        assertThat(realServlet.organisation, is("acme corp"));

        ShariffBackend backend = realServlet.createBackend();
        assertThat(backend.getTargets().size(), is(2));
    }

}
