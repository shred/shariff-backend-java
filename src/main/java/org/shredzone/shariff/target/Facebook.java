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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.shredzone.shariff.api.JSONTarget;
import org.shredzone.shariff.api.TargetName;

/**
 * Facebook target.
 *
 * @author Richard "Shred" Körber
 */
@TargetName("facebook")
public class Facebook extends JSONTarget {

    private static final String API_VERSION = "v2.8";
    private static final String UTF_8 = "utf-8";

    private String clientId;
    private String secret;
    private String accessToken = null;

    /**
     * Sets the credentials for retrieving an access token.
     * <p>
     * This method must be invoked in order to get an access counter from Facebook. If not
     * set, a zero count will be returned.
     *
     * @param clientId
     *            Client ID
     * @param secret
     *            Client Secret
     */
    public void setSecret(String clientId, String secret) {
        this.clientId = clientId;
        this.secret = secret;
        this.accessToken = null; // invalidate current access token
    }

    @Override
    public int count(String url) throws IOException {
        if (clientId == null || secret == null) {
            throw new IllegalStateException("You need to set a clientId and a secret");
        }
        return super.count(url);
    }

    @Override
    protected HttpURLConnection connect(String url) throws IOException {
        URL connectUrl = new URL("https://graph.facebook.com/" + API_VERSION +"/"
                        + "?id=" + URLEncoder.encode(url, UTF_8)
                        + "&" + getAccessToken());
        return openConnection(connectUrl);
    }

    @Override
    protected int extractCount(JSONTokener json) {
        JSONObject jo = (JSONObject) json.nextValue();
        if (jo.has("share")) {
            return jo.getJSONObject("share").getInt("share_count");
        } else {
            return 0;
        }
    }

    protected synchronized String getAccessToken() throws IOException {
        if (accessToken == null) {
            URL tokenUrl = new URL("https://graph.facebook.com/oauth/access_token"
                            + "?client_id=" + URLEncoder.encode(clientId, UTF_8)
                            + "&client_secret=" + URLEncoder.encode(secret, UTF_8)
                            + "&grant_type=client_credentials");
            HttpURLConnection connection = openConnection(tokenUrl);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + connection.getResponseCode() + ": " + connection.getResponseMessage());
            }

            try (BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {
                accessToken = in.readLine(); // only one line is expected
            } finally {
                connection.disconnect();
            }
        }
        return accessToken;
    }

}
