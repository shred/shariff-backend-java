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

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A backend service for performing Shariff requests.
 *
 * @author Richard "Shred" Körber
 */
public class ShariffBackend {
    private static final Logger LOG = LoggerFactory.getLogger(ShariffBackend.class);

    private final List<Target> targets;
    private final ExecutorService executor;

    /**
     * Creates a new backend instance.
     */
    public ShariffBackend() {
        this(null, null);
    }

    /**
     * Creates a new backend instance, only providing the given targets.
     *
     * @param names
     *            Target names, as returned by {@link Target#getName()}. Unknown names
     *            will be silently ignored.
     */
    public ShariffBackend(Collection<String> names) {
        this(names, null);
    }

    /**
     * Creates a new backend instance, only providing the given targets. The maximum
     * number of threads can be set as well, also limiting the number of simultaneous HTTP
     * connections.
     *
     * @param names
     *            Target names, as returned by {@link Target#getName()}. Unknown names
     *            will be silently ignored. {@code null} means all targets.
     * @param maxThreads
     *            Maximum number of threads. {@code null} means as many threads as there
     *            are targets.
     */
    public ShariffBackend(Collection<String> names, Integer maxThreads) {
        List<Target> list = createTargets().stream()
                    .filter(target -> names == null || names.contains(target.getName()))
                    .collect(toList());
        targets = Collections.unmodifiableList(list);

        ThreadGroup group = new ThreadGroup("shariff");
        executor =  Executors.newFixedThreadPool(
                maxThreads != null ? maxThreads : targets.size(),
                runnable -> {
                    Thread t = new Thread(group, runnable);
                    t.setDaemon(true);
                    return t;
                });
    }

    /**
     * Returns a list of known {@link Target} instances.
     */
    protected List<Target> createTargets() {
        return Arrays.<Target>asList(
                new AddThis(),
                new Facebook(),
                new Flattr(),
                new GooglePlus(),
                new LinkedIn(),
                new Pinterest(),
                new Reddit(),
                new StumbleUpon(),
                new Xing()
        );
    }

    /**
     * Returns all available Shariff targets.
     */
    public List<Target> getTargets() {
        return targets;
    }

    /**
     * Returns the Shariff target instance of the given type.
     *
     * @param type
     *            {@link Target} type
     * @return Instance, or {@code null} if there is no such target available
     */
    public <T extends Target> T getTarget(Class<T> type) {
        return targets.stream()
                .filter(type::isInstance)
                .findFirst()
                .map(type::cast)
                .orElse(null);
    }

    /**
     * Retrieves the counters for the given URL and returns a map of all Shariff targets
     * and the returned counters.
     *
     * @param url
     *            URL to get the counters for
     * @return Map containing the target name as key, and the counter as value
     */
    public Map<String, Integer> getCounts(String url) {
        List<Future<Integer>> futures = getTargets().stream()
            .map(target -> executor.submit(() -> target.count(url)))
            .collect(toList());

        Map<String, Integer> result = new HashMap<>();
        for (int ix = 0; ix < futures.size(); ix++) {
            Target target = getTargets().get(ix);
            try {
                result.put(target.getName(), futures.get(ix).get());
            } catch (ExecutionException ex) {
                LOG.trace("Caught exception from {}", target.getName(), ex);
                LOG.warn("{} @ {}", target.getName(), url, ex.getCause());
            } catch (Exception ex) {
                LOG.warn("{} @ {}", target.getName(), url, ex);
            }
        }

        return result;
    }

    /**
     * A small tool that shows the counters for the given URL.
     */
    public static void main(String... args) {
        if (args.length < 1) {
            System.err.println("Usage: ShariffBackend <url> ...");
            System.err.println("Facebook: use system properties 'facebook.id' and 'facebook.secret'");
            return;
        }

        ShariffBackend backend = new ShariffBackend();
        Facebook fb = backend.getTarget(Facebook.class);
        fb.setSecret(System.getProperty("facebook.id"), System.getProperty("facebook.secret"));

        for (String url : args) {
            System.out.println(url);

            backend.getCounts(url).forEach((key, value) ->
                    System.out.println(String.format("  %-12s: %d", key, value)));

            System.out.println();
        }
    }

}
