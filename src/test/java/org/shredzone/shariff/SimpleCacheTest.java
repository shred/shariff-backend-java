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

import org.junit.Test;

/**
 * Unit tests for {@link SimpleCache}.
 *
 * @author Richard "Shred" Körber
 */
public class SimpleCacheTest {

    @Test
    public void maxEntriesTest() {
        SimpleCache<Integer, String> cache = new SimpleCache<>(10, 10000L);

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
    public void timeoutTest() throws InterruptedException {
        final long timeout = 750L;

        SimpleCache<Integer, Object> cache = new SimpleCache<>(10, timeout);

        for (int ix = 0; ix < 10; ix++) {
            cache.put(ix, new Object());
        }
        assertThat(cache.size(), is(10));
        for (int ix = 0; ix < 10; ix++) {
            assertThat(cache.get(ix), is(notNullValue()));
        }

        Thread.sleep(timeout + 10L); // Add a safety margin

        for (int ix = 0; ix < 10; ix++) {
            assertThat(cache.get(ix), is(nullValue()));
        }
        assertThat(cache.size(), is(0));
    }

}
