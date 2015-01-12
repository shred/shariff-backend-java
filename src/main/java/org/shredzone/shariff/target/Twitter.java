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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Twitter target.
 *
 * @author Richard "Shred" Körber
 */
public class Twitter extends JSONTarget<JSONObject> {

    @Override
    public String getName() {
        return "twitter";
    }

    @Override
    protected HttpURLConnection connect(String url) throws IOException {
        URL connectUrl = new URL("https://cdn.api.twitter.com/1/urls/count.json?url="
                        + URLEncoder.encode(url, "utf-8"));

        return openConnection(connectUrl);
    }

    @Override
    protected int extractCount(JSONObject json) throws JSONException {
        return json.getInt("count");
    }

}
