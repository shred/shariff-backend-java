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
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.shredzone.shariff.api.JSONTarget;
import org.shredzone.shariff.api.TargetName;

/**
 * Google Plus target.
 *
 * @author Richard "Shred" Körber
 */
@TargetName("googleplus")
public class GooglePlus extends JSONTarget {

    private static final String GOOGLE_KEY = "AIzaSyCKSbrvQasunBoV16zDH9R33D88CeLr9gQ";

    @Override
    protected HttpURLConnection connect(String url) throws IOException {
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
    }

    @Override
    protected int extractCount(JSONTokener json) {
        JSONObject jo = (JSONObject) json.nextValue();
        return jo.getJSONObject("result")
                        .getJSONObject("metadata")
                        .getJSONObject("globalCounts")
                        .getInt("count");
    }

}
