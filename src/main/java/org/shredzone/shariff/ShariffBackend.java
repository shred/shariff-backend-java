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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.shredzone.shariff.target.AddThis;
import org.shredzone.shariff.target.Facebook;
import org.shredzone.shariff.target.Flattr;
import org.shredzone.shariff.target.GooglePlus;
import org.shredzone.shariff.target.LinkedIn;
import org.shredzone.shariff.target.Pinterest;
import org.shredzone.shariff.target.Reddit;
import org.shredzone.shariff.target.StumbleUpon;
import org.shredzone.shariff.target.Target;
import org.shredzone.shariff.target.Xing;

/**
 * A backend service for performing Shariff requests.
 *
 * @author Richard "Shred" Körber
 */
public class ShariffBackend {

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
        List<Target> list = new ArrayList<>(createTargets());

        if (names != null) {
            Iterator<Target> it = list.iterator();
            while (it.hasNext()) {
                if (!names.contains(it.next().getName())) {
                    it.remove();
                }
            }
        }

        final ThreadGroup group = new ThreadGroup("shariff");

        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(group, r);
                t.setDaemon(true);
                return t;
            }
        };

        targets = Collections.unmodifiableList(list);
        executor =  Executors.newFixedThreadPool(
                        (maxThreads != null ? maxThreads : targets.size()),
                        factory);
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
    @SuppressWarnings("unchecked")
    public <T extends Target> T getTarget(Class<T> type) {
        for (Target t : targets) {
            if (type.isInstance(t)) {
                return (T) t;
            }
        }
        return null;
    }

    /**
     * Retrieves the counters for the given URL and returns a map of all Shariff targets
     * and the returned counters.
     *
     * @param url
     *            URL to get the counters for
     * @return Map containing the target name as key, and the counter as value
     */
    public Map<String, Integer> getCounts(final String url) {
        List<Future<Integer>> futures = new ArrayList<>(getTargets().size());

        for (final Target target : getTargets()) {
            futures.add(executor.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return target.count(url);
                }
            }));
        }

        Map<String, Integer> result = new HashMap<>();
        for (int ix = 0; ix < futures.size(); ix++) {
            Target target = getTargets().get(ix);
            try {
                result.put(target.getName(), futures.get(ix).get());
            } catch (Exception ex) {
                Logger.getLogger(ShariffBackend.class.getName()).log(
                        Level.WARNING,
                        target.getName() + " @ " + url,
                        (ex instanceof ExecutionException ? ((ExecutionException) ex).getCause() : ex));
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

            Map<String, Integer> result = backend.getCounts(url);
            for (Map.Entry<String, Integer> entry : result.entrySet()) {
                System.out.println(String.format("  %-12s: %d", entry.getKey(), entry.getValue()));
            }

            System.out.println();
        }
    }

}
