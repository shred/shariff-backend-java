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
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Reddit target.
 *
 * @author Richard "Shred" Körber
 */
public class Reddit extends JSONTarget<JSONObject> {

    @Override
    public String getName() {
        return "reddit";
    }

    @Override
    protected HttpURLConnection connect(String url) throws IOException {
        URL connectUrl = new URL("https://www.reddit.com/api/info.json?url="
                        + URLEncoder.encode(url, "utf-8"));

        return openConnection(connectUrl);
    }

    @Override
    protected int extractCount(JSONObject json) {
        int total = 0;

        JSONArray children = json.getJSONObject("data").getJSONArray("children");
        for (int ix = 0; ix < children.length(); ix++) {
            total += children.getJSONObject(ix).getJSONObject("data").getInt("score");
        }

        return total;
    }

}
