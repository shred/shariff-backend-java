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
package org.shredzone.shariff;

import java.io.IOException;

/**
 * This exception is thrown when the target server responded that a request rate limit has
 * been exceeded.
 *
 * @since 1.7
 * @author Richard "Shred" Körber
 */
public class RateLimitExceededException extends IOException {
    private static final long serialVersionUID = -1918732234956909887L;

    /**
     * Creates a new {@link RateLimitExceededException}.
     */
    public RateLimitExceededException() {
        super();
    }

    /**
     * Creates a new {@link RateLimitExceededException}.
     *
     * @param message
     *            A message giving a hint about the rate limit that was exceeded.
     */
    public RateLimitExceededException(String message) {
        super(message);
    }

}
