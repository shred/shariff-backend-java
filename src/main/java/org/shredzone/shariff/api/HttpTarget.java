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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

import org.shredzone.shariff.RateLimitExceededException;

/**
 * A base {@link Target} implementation that performs HTTP requests.
 *
 * @author Richard "Shred" Körber
 */
public abstract class HttpTarget implements Target {

    private static final int HTTP_TIMEOUT_MS = 10000;
    private static final String BACKEND_VERSION;

    static {
        StringBuilder version = new StringBuilder("shariff-backend-java");
        try (InputStream in = HttpTarget.class.getResourceAsStream("/org/shredzone/shariff/version.properties")){
            Properties prop = new Properties();
            prop.load(in);
            version.append('/').append(prop.getProperty("version"));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        version.append(" Java/").append(System.getProperty("java.version"));
        BACKEND_VERSION = version.toString();
    }

    private String userAgent = BACKEND_VERSION;

    @Override
    public int count(String url) throws IOException {
        HttpURLConnection connection = connect(url);
        try {
            checkResponse(connection);
            try (InputStream in = connection.getInputStream()) {
                return extractCount(in);
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Checks if the {@link HttpURLConnection} contains a valid response.
     *
     * @param connection
     *            {@link HttpURLConnection} to check
     * @throws IOException
     *             if the response was invalid
     * @throws RateLimitExceededException
     *             if the response shows that a rate limit has been exceeded
     * @since 1.7
     */
    protected void checkResponse(HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP " + connection.getResponseCode() + ": " + connection.getResponseMessage());
        }
    }

    /**
     * Sets an optional organisation name that is attached to the User-Agent header.
     *
     * @param organisation
     *            Organisation name, or {@code null} if unset
     * @since 1.7
     */
    public void setOrganisation(String organisation) {
        if (organisation != null) {
            userAgent = BACKEND_VERSION + " (" + organisation + ")";
        } else {
            userAgent = BACKEND_VERSION;
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
        String connectUrl = template.replace("{}", URLEncoder.encode(url, UTF_8.name()));
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
        connection.setRequestProperty("User-Agent", getUserAgent());
        return connection;
    }

    /**
     * Returns the User-Agent to be used for HTTP connections.
     *
     * @since 1.7
     */
    protected String getUserAgent() {
        return userAgent;
    }

}
