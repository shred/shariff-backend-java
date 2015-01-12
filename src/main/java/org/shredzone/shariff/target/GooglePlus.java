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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Google Plus target.
 *
 * @author Richard "Shred" Körber
 */
public class GooglePlus extends JSONTarget<JSONObject> {

    private final static String GOOGLE_KEY = "AIzaSyCKSbrvQasunBoV16zDH9R33D88CeLr9gQ";

    @Override
    public String getName() {
        return "googleplus";
    }

    @Override
    protected HttpURLConnection connect(String url) throws IOException {
        try {
            URL connectUrl = new URL("https://clients6.google.com/rpc?key=" + GOOGLE_KEY);

            HttpURLConnection connection = openConnection(connectUrl);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try (OutputStream out = connection.getOutputStream()) {
                JSONObject json = new JSONObject();

                json.put("method", "pos.plusones.get")
                    .put("id", "p")
                    .put("params", new JSONObject()
                        .put("nolog", "true")
                        .put("id", url)
                        .put("source", "widget")
                        .put("userId", "@viewer")
                        .put("groupId", "@self")
                    )
                    .put("jsonrpc", "2.0")
                    .put("key", "p")
                    .put("apiVersion", "v1");

                out.write(json.toString().getBytes("utf-8"));
            }

            return connection;
        } catch (MalformedURLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected int extractCount(JSONObject json) throws JSONException {
        return json.getJSONObject("result")
                        .getJSONObject("metadata")
                        .getJSONObject("globalCounts")
                        .getInt("count");
    }

}
