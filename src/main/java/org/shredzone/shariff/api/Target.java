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

/**
 * A target for getting the URL counter from.
 * <p>
 * Targets usually perform a HTTP request to a web service. However, {@link Target}
 * implementations could also fetch the data from a database or other sources.
 *
 * @author Richard "Shred" Körber
 */
public interface Target {

    /**
     * Default implementation that returns the target name from the {@link TargetName}
     * annotation.
     *
     * @return name of the target
     */
    default String getName() {
        return getClass().getAnnotation(TargetName.class).value();
    }

    /**
     * Fetches the counter of the target for the given URL.
     *
     * @param url
     *            URL to get the counter of
     * @return Click counter for this url
     * @throws IOException
     *             if the counter could not be retrieved
     */
    int count(String url) throws IOException;

}
