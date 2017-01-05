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

/**
 * A target for getting the URL counter from.
 *
 * @author Richard "Shred" Körber
 */
public interface Target {

    /**
     * @return name of the target
     */
    String getName();

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
