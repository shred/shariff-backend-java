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

    private static final String API_VERSION = "v3.0";
    private static final String UTF_8 = "utf-8";

    private String appId;
    private String appSecret;

    /**
     * Sets the credentials for retrieving an access token.
     * <p>
     * This method must be invoked in order to get an access counter from Facebook. If not
     * set, a zero count will be returned.
     *
     * @param appId
     *            App ID
     * @param appSecret
     *            App Secret
     */
    public void setSecret(String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
    }

    @Override
    public int count(String url) throws IOException {
        if (appId == null || appSecret == null) {
            throw new IllegalStateException("You need to set an app id and an app secret");
        }
        return super.count(url);
    }

    @Override
    protected HttpURLConnection connect(String url) throws IOException {
        URL connectUrl = new URL("https://graph.facebook.com/" + API_VERSION
                        + "/?id=" + URLEncoder.encode(url, UTF_8)
                        + "&fields=engagement"
                        + "&access_token=" + URLEncoder.encode(appId, UTF_8)
                        + '|' + URLEncoder.encode(appSecret, UTF_8));
        return openConnection(connectUrl);
    }

    @Override
    protected int extractCount(JSONTokener json) {
        JSONObject jo = (JSONObject) json.nextValue();
        if (jo.has("engagement")) {
            JSONObject eo = jo.getJSONObject("engagement");
            return eo.optInt("reaction_count")
                    + eo.optInt("comment_count")
                    + eo.optInt("share_count");
        } else {
            return 0;
        }
    }

}
