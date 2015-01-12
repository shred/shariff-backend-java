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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * A servlet that reads the "url" parameter and returns a JSON object containing the
 * target counters.
 *
 * @author Richard "Shred" Körber
 */
public class ShariffServlet extends HttpServlet {
    private static final long serialVersionUID = 3246833254367746537L;

    private transient ShariffBackend backend = null;
    private transient SimpleCache<String, Map<String, Integer>> cache = null;

    private Pattern hostPattern = null;
    private int cacheSize = 1000;
    private long timeToLiveMs = 60000L;
    private String[] targets = null;

    /**
     * Generates a {@link ShariffBackend}.
     * <p>
     * Override for creating custom configured {@link ShariffBackend} instances.
     */
    protected ShariffBackend createBackend() {
        if (targets != null) {
            return new ShariffBackend(Arrays.asList(targets));
        } else {
            return new ShariffBackend();
        }
    }

    /**
     * Returns the counters for the given url. The result is not cached.
     * <p>
     * Override for using own counter implementations.
     *
     * @param url
     *            URL to get the counters of
     * @return Map of counters
     */
    protected Map<String, Integer> getCounts(String url) throws IOException {
        return backend.getCounts(url);
    }

    /**
     * Returns the counters for the given url. The result is cached.
     * <p>
     * The default implementation uses simple in-memory caching. Override for own caching
     * implementations.
     *
     * @param url
     *            URL to get the counters of
     * @return Map of counters
     */
    protected Map<String, Integer> getCountsCached(String url) throws IOException {
        synchronized (this) {
            if (cache == null) {
                cache = new SimpleCache<>(cacheSize, timeToLiveMs);
            }

            Map<String, Integer> result = cache.get(url);
            if (result == null) {
                result = getCounts(url);
                cache.put(url, result);
            }

            return result;
        }
    }

    /**
     * Checks if the given URL is a valid host. It tests for a valid URL syntax.
     * <p>
     * When a "host" servlet parameter was given, it also tests if it pattern matches.
     * Otherwise only the servlet's host name is accepted.
     * <p>
     * Override for individual tests.
     *
     * @param url
     *            URL to check
     * @param req
     *            {@link HttpServletResquest} of the request
     * @return {@code true} if this is a valid host
     */
    protected boolean isValidHost(String url, HttpServletRequest req) {
        try {
            URL requestUrl = new URL(url);

            if (hostPattern != null) {
                return hostPattern.matcher(requestUrl.getHost()).matches();

            } else {
                return requestUrl.getHost().equalsIgnoreCase(req.getServerName());

            }
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String hosts = config.getInitParameter("host");
        if (hosts != null) {
            hostPattern = Pattern.compile(hosts, Pattern.CASE_INSENSITIVE);
        }

        String cs = config.getInitParameter("cacheSize");
        if (cs != null) {
            cacheSize = Integer.parseInt(cs);
        }

        String ttl = config.getInitParameter("cacheTimeToLiveMs");
        if (ttl != null) {
            timeToLiveMs = Long.parseLong(ttl);
        }

        String trg = config.getInitParameter("targets");
        if (trg != null) {
            targets = trg.split("[,:;]+");
            for (int ix = 0; ix < targets.length; ix++) {
                targets[ix] = targets[ix].trim();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        String url = req.getParameter("url");

        if (url == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing url parameter");
            return;
        }

        if (!isValidHost(url, req)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid host");
            return;
        }

        if (backend == null) {
            backend = createBackend();
        }

        JSONObject json = new JSONObject();
        for (Map.Entry<String, Integer> count : getCountsCached(url).entrySet()) {
            json.put(count.getKey(), count.getValue());
        }

        resp.setContentType("Content-type: application/json");
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().append(json.toString());
    }

}
