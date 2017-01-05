/*
 * shariff-backend-java
 *
 * Copyright (C) 2017 Richard "Shred" Körber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.shariff.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * A base {@link Target} implementation that performs HTTP requests.
 *
 * @author Richard "Shred" Körber
 */
public abstract class HttpTarget implements Target {

    private static final int HTTP_TIMEOUT_MS = 10000;
    private static final String USER_AGENT;

    static {
        StringBuilder agent = new StringBuilder("shariff-backend-java");
        try {
            Properties prop = new Properties();
            prop.load(HttpTarget.class.getResourceAsStream("/org/shredzone/shariff/version.properties"));
            agent.append('/').append(prop.getProperty("version"));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        agent.append(" Java/").append(System.getProperty("java.version"));

        USER_AGENT = agent.toString();
    }

    @Override
    public int count(String url) throws IOException {
        HttpURLConnection connection = connect(url);

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP " + connection.getResponseCode() + ": " + connection.getResponseMessage());
        }

        try (InputStream in = connection.getInputStream()) {
            return extractCount(in);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Extracts the click counter from the API response.
     *
     * @param in
     *            {@link InputStream} of what was returned from the API call
     * @return Click counter
     */
    protected abstract int extractCount(InputStream in) throws IOException;

    /**
     * Connect to the target.
     * <p>
     * The default implementation uses the URL template as defined in the
     * {@link TargetUrl} annotation, and replaces the "{}" placeholder with the given URL.
     *
     * @param url
     *            URL to retrieve the counter for
     * @return {@link HttpURLConnection} that is connected to the API call
     * @throws IOException
     *             if the connection could not be established
     */
    protected HttpURLConnection connect(String url) throws IOException {
        String template = getClass().getAnnotation(TargetUrl.class).value();
        String connectUrl = template.replace("{}", URLEncoder.encode(url, "utf-8"));
        return openConnection(new URL(connectUrl));
    }

    /**
     * Opens a connection to the given service API url.
     * <p>
     * This method can be overridden for unit testing purposes or for configurating the
     * {@link HttpURLConnection}.
     *
     * @param url
     *            {@link URL} to connect to
     * @return {@link HttpURLConnection} to that URL
     * @throws IOException
     *             if the connection could not be opened
     */
    protected HttpURLConnection openConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(false);
        connection.setConnectTimeout(HTTP_TIMEOUT_MS);
        connection.setReadTimeout(HTTP_TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        return connection;
    }

}
