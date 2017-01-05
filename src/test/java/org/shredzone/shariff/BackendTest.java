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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.shredzone.shariff.api.Target;
import org.shredzone.shariff.target.AddThis;
import org.shredzone.shariff.target.Facebook;
import org.shredzone.shariff.target.Flattr;
import org.shredzone.shariff.target.GooglePlus;
import org.shredzone.shariff.target.LinkedIn;
import org.shredzone.shariff.target.Pinterest;
import org.shredzone.shariff.target.Reddit;
import org.shredzone.shariff.target.StumbleUpon;
import org.shredzone.shariff.target.Xing;

/**
 * Unit tests for {@link BackendTest}.
 *
 * @author Richard "Shred" Körber
 */
public class BackendTest {

    private static final String TEST_URL = "http://www.heise.de";

    @SuppressWarnings("unchecked")
    @Test
    public void getTargetTest() {
        ShariffBackend backend = new ShariffBackend();
        Collection<Target> targets = backend.getTargets();
        assertThat(targets, contains(
                instanceOf(AddThis.class),
                instanceOf(Facebook.class),
                instanceOf(Flattr.class),
                instanceOf(GooglePlus.class),
                instanceOf(LinkedIn.class),
                instanceOf(Pinterest.class),
                instanceOf(Reddit.class),
                instanceOf(StumbleUpon.class),
                instanceOf(Xing.class)
        ));

        Flattr flattrTarget = backend.getTarget(Flattr.class);
        assertThat(flattrTarget, is(notNullValue()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getTargetByConstructorTest() {
        ShariffBackend backend = new ShariffBackend(Arrays.asList("facebook", "flattr"));
        Collection<Target> targets = backend.getTargets();
        assertThat(targets, contains(
                instanceOf(Facebook.class),
                instanceOf(Flattr.class)
        ));
    }

    @Test
    public void getCountsTest() throws IOException {
        ShariffBackend backend = new ShariffBackend() {
            @Override
            protected List<Target> createTargets() {
                return Arrays.<Target>asList(
                        new TestTarget("facebook", 10),
                        new TestTarget("googleplus", 20),
                        new TestTarget("flattr", 30)
                );
            }
        };

        Map<String, Integer> counts = backend.getCounts(TEST_URL);

        assertThat(counts.size(), is(3));
        assertThat(counts.get("facebook"), is(10));
        assertThat(counts.get("googleplus"), is(20));
        assertThat(counts.get("flattr"), is(30));
    }

    /**
     * A mock {@link Target} that returns a fixed name and counter.
     */
    private static class TestTarget implements Target {
        private final String name;
        private final int count;

        public TestTarget(String name, int count) {
            this.name = name;
            this.count = count;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int count(String url) throws IOException {
            assertThat(url, is(TEST_URL));
            return count;
        }
    }

}
