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
package org.shredzone.shariff.api;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONTokener;

/**
 * An abstract {@link Target} for reading JSON responses.
 *
 * @author Richard "Shred" Körber
 */
public abstract class JSONTarget extends HttpTarget {

    @Override
    protected int extractCount(InputStream in) throws IOException {
        return extractCount(new JSONTokener(in));
    }

    /**
     * Extracts the click counter from the API connection.
     *
     * @param json
     *            decoded JSON that was returned by the API
     * @return Click counter
     */
    protected abstract int extractCount(JSONTokener json);

}
