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

import org.json.JSONObject;
import org.json.JSONTokener;
import org.shredzone.shariff.api.JSONTarget;
import org.shredzone.shariff.api.TargetName;
import org.shredzone.shariff.api.TargetUrl;

/**
 * AddThis target.
 *
 * @author Richard "Shred" Körber
 */
@TargetName("addthis")
@TargetUrl("https://api-public.addthis.com/url/shares.json?url={}")
public class AddThis extends JSONTarget {

    @Override
    protected int extractCount(JSONTokener json) {
        JSONObject jo = (JSONObject) json.nextValue();
        return jo.optInt("shares");
    }

}
