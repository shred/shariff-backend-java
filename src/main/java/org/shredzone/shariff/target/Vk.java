/*
 * shariff-backend-java
 *
 * Copyright (C) 2018 Richard "Shred" Körber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.shariff.target;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.shredzone.shariff.api.HttpTarget;
import org.shredzone.shariff.api.TargetName;
import org.shredzone.shariff.api.TargetUrl;

/**
 * Vk target.
 *
 * @author Richard "Shred" Körber
 */
@TargetName("vk")
@TargetUrl("https://vk.com/share.php?act=count&index=1&url={}")
public class Vk extends HttpTarget {

    private static final Pattern COUNTER = Pattern.compile(
                    Pattern.quote("VK.Share.count") + "\\(.*?,\\s*(\\d+)\\);.*");

    @Override
    protected int extractCount(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in, UTF_8));
        String line = r.readLine();
        Matcher m = COUNTER.matcher(line);
        if (!m.matches()) {
            throw new IOException("Cannot parse counter: '" + line + "'");
        }
        return Integer.parseInt(m.group(1));
    }

}
