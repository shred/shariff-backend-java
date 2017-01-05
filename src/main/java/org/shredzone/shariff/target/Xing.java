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
import java.net.URLEncoder;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.shredzone.shariff.api.JSONTarget;
import org.shredzone.shariff.api.TargetName;

/**
 * Xing target.
 *
 * @author Richard "Shred" Körber
 */
@TargetName("xing")
public class Xing extends JSONTarget {

    @Override
    protected HttpURLConnection connect(String url) throws IOException {
        URL connectUrl = new URL("https://www.xing-share.com/spi/shares/statistics");

        HttpURLConnection connection = openConnection(connectUrl);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        StringBuilder sb = new StringBuilder();
        sb.append("url=");
        sb.append(URLEncoder.encode(url, "utf-8"));

        try (OutputStream out = connection.getOutputStream()) {
            out.write(sb.toString().getBytes("utf-8"));
        }

        return connection;
    }

    @Override
    protected int extractCount(JSONTokener json) {
        JSONObject jo = (JSONObject) json.nextValue();
        return jo.getInt("share_counter");
    }

}
