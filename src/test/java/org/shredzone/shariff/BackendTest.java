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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.shredzone.shariff.api.Target;
import org.shredzone.shariff.target.AddThis;
import org.shredzone.shariff.target.Buffer;
import org.shredzone.shariff.target.Facebook;
import org.shredzone.shariff.target.Pinterest;
import org.shredzone.shariff.target.Reddit;
import org.shredzone.shariff.target.StumbleUpon;
import org.shredzone.shariff.target.Vk;
import org.shredzone.shariff.target.Xing;

/**
 * Unit tests for {@link BackendTest}.
 *
 * @author Richard "Shred" Körber
 */
public class BackendTest {

    private static final String TEST_URL = "http://www.heise.de";

    @Test
    public void getTargetTest() {
        ShariffBackend backend = new ShariffBackend();
        Collection<Target> targets = backend.getTargets();
        assertThat(targets, contains(
                instanceOf(AddThis.class),
                instanceOf(Buffer.class),
                instanceOf(Facebook.class),
                instanceOf(Pinterest.class),
                instanceOf(Reddit.class),
                instanceOf(StumbleUpon.class),
                instanceOf(Vk.class),
                instanceOf(Xing.class)
        ));

        Facebook facebookTarget = backend.getTarget(Facebook.class);
        assertThat(facebookTarget, is(notNullValue()));
    }

    @Test
    public void getTargetByConstructorTest() {
        ShariffBackend backend = new ShariffBackend(Arrays.asList("facebook", "reddit"));
        Collection<Target> targets = backend.getTargets();
        assertThat(targets, contains(
                instanceOf(Facebook.class),
                instanceOf(Reddit.class)
        ));
    }

    @Test
    public void getUnknownTargetTest() {
        ShariffBackend backend = new ShariffBackend(Arrays.asList("facebook", "reddit", "mockr"));
        Collection<Target> targets = backend.getTargets();
        assertThat(targets, contains(
                instanceOf(Facebook.class),
                instanceOf(Reddit.class)
        ));
    }

    @Test
    public void getCountsTest() {
        ShariffBackend backend = new ShariffBackend() {
            @Override
            protected List<Target> createTargets() {
                return Arrays.asList(
                        new TestTarget("facebook", 10),
                        new TestTarget("reddit", 20),
                        new TestTarget("xing", 30)
                );
            }
        };

        Map<String, Integer> counts = backend.getCounts(TEST_URL);

        assertThat(counts.size(), is(3));
        assertThat(counts.get("facebook"), is(10));
        assertThat(counts.get("reddit"), is(20));
        assertThat(counts.get("xing"), is(30));
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
        public int count(String url) {
            assertThat(url, is(TEST_URL));
            return count;
        }
    }

}
