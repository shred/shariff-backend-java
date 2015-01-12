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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Facebook target.
 *
 * @author Richard "Shred" Körber
 */
public class Facebook extends JSONTarget<JSONArray> {

    @Override
    public String getName() {
        return "facebook";
    }

    @Override
    protected HttpURLConnection connect(String url) throws IOException {
        try {
            URL connectUrl = new URL("https://api.facebook.com/method/fql.query"
                            + "?format=json"
                            + "&query="
                            + URLEncoder.encode("select share_count from link_stat where url=\"" + url + "\"", "utf-8"));

            return openConnection(connectUrl);
        } catch (MalformedURLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected int extractCount(JSONArray json) throws JSONException {
        return json.getJSONObject(0).getInt("share_count");
    }

}
