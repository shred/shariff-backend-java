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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONTokener;

/**
 * An abstract {@link Target} for reading JSON responses.
 *
 * @author Richard "Shred" Körber
 */
public abstract class JSONTarget<T> implements Target {

    private static final int HTTP_TIMEOUT_MS = 10000;

    @Override
    public int count(String url) throws IOException {
        HttpURLConnection connection = connect(url);

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP " + connection.getResponseCode() + ": " + connection.getResponseMessage());
        }

        try {
            try (InputStream in = connection.getInputStream()) {
                return extractCount(read(in));
            } catch (JSONException ex) {
                throw new IllegalArgumentException(ex);
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Connect to the target.
     *
     * @param url
     *            URL to retrieve the counter for
     * @return {@link HttpURLConnection} that is connected to the API call
     * @throws IOException
     *             if the connection could not be established
     */
    protected abstract HttpURLConnection connect(String url) throws IOException;

    /**
     * Extracts the click counter from the API connection.
     *
     * @param json
     *            decoded JSON that was returned by the API
     * @return Click counter
     * @throws JSONException
     *             if the JSON could not be read or the counter field was missing
     */
    protected abstract int extractCount(T json) throws JSONException;

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
        return connection;
    }

    /**
     * Reads data from input stream and provides a result object.
     *
     * @param in
     *            {@link InputStream} to read from
     * @return Read object
     */
    @SuppressWarnings("unchecked")
    protected T read(InputStream in) throws IOException {
        // This is ugly, but there is no supertype for JSONObject and JSONArray
        return (T) new JSONTokener(in).nextValue();
    }

}
