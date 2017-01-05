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
package org.shredzone.shariff;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

/**
 * Unit tests for {@link SimpleCache}.
 *
 * @author Richard "Shred" Körber
 */
public class SimpleCacheTest {

    @Test
    public void maxEntriesTest() {
        SimpleCache<Integer, String> cache = new SimpleCache<>(10, 5, TimeUnit.SECONDS);

        for (int ix = 0; ix < 20; ix++) {
            cache.put(ix, String.valueOf(ix));
            assertThat(cache.size(), is(Math.min(ix + 1, 10)));
        }

        assertThat(cache.size(), is(10));

        int count = 0;
        for (int ix = 0; ix < 20; ix++) {
            String data = cache.get(ix);
            if (data != null) {
                assertThat(data, is(String.valueOf(ix)));
                count++;
            }
        }
        assertThat(count, is(10));
    }

    @Test
    public void timeoutTest() {
        AtomicBoolean expired = new AtomicBoolean(false);

        SimpleCache<Integer, Object> cache = new SimpleCache<Integer, Object>(10, 5, TimeUnit.SECONDS) {
            @Override
            protected boolean isExpired(long expiry) {
                return expired.get();
            }
        };

        // Fill with 10 entries
        for (int ix = 0; ix < 10; ix++) {
            cache.put(ix, new Object());
        }

        // Not expired yet: make sure all entries are present
        assertThat(cache.size(), is(10));
        for (int ix = 0; ix < 10; ix++) {
            assertThat(cache.get(ix), is(notNullValue()));
        }

        // Now expire all records
        expired.set(true);

        // Make sure all entries are gone
        for (int ix = 0; ix < 10; ix++) {
            assertThat(cache.get(ix), is(nullValue()));
        }
        assertThat(cache.size(), is(0));
    }

    @Test
    public void expireTest() {
        SimpleCache<Integer, Object> cache = new SimpleCache<>(10, 5, TimeUnit.SECONDS);

        long now = System.currentTimeMillis();

        assertThat(cache.isExpired(now - 500L), is(true));
        assertThat(cache.isExpired(now + 500L), is(false));
    }

}
