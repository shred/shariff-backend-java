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

import java.io.IOException;
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

import org.shredzone.shariff.target.Facebook;
import org.shredzone.shariff.target.Flattr;
import org.shredzone.shariff.target.GooglePlus;
import org.shredzone.shariff.target.LinkedIn;
import org.shredzone.shariff.target.Pinterest;
import org.shredzone.shariff.target.Target;
import org.shredzone.shariff.target.Twitter;

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
        final ThreadGroup group = new ThreadGroup("shariff");

        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(group, r);
                t.setDaemon(true);
                return t;
            }
        };

        targets = Collections.unmodifiableList(createTargets());
        executor = Executors.newFixedThreadPool(targets.size(), factory);
    }

    /**
     * Creates a new backend instance, only providing the given targets.
     *
     * @param names
     *            Target names, as returned by {@link Target#getName()}. Unknown names
     *            will be silently ignored.
     */
    public ShariffBackend(Collection<String> names) {
        List<Target> list = new ArrayList<>(createTargets());

        Iterator<Target> it = list.iterator();
        while (it.hasNext()) {
            if (!names.contains(it.next().getName())) {
                it.remove();
            }
        }

        targets = Collections.unmodifiableList(list);
        executor =  Executors.newFixedThreadPool(targets.size());
    }

    /**
     * Returns a list of known {@link Target} instances.
     */
    protected List<Target> createTargets() {
        return Arrays.<Target>asList(
                new Facebook(),
                new Flattr(),
                new GooglePlus(),
                new LinkedIn(),
                new Pinterest(),
                new Twitter()
        );
    }

    /**
     * Returns all available Shariff targets.
     */
    public List<Target> getTargets() {
        return targets;
    }

    /**
     * Retrieves the counters for the given URL and returns a map of all Shariff targets
     * and the returned counters.
     *
     * @param url
     *            URL to get the counters for
     * @return Map containing the target name as key, and the counter as value
     * @throws IOException
     *             if at least one of the counters could not be retrieved
     */
    public Map<String, Integer> getCounts(final String url) throws IOException {
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
        try {
            for (int ix = 0; ix < futures.size(); ix++) {
                result.put(getTargets().get(ix).getName(), futures.get(ix).get());
            }
        } catch (ExecutionException | InterruptedException ex) {
            if (ex.getCause() != null && ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            }
            throw new IOException(ex);
        }

        return result;
    }

    /**
     * A small tool that shows the counters for the given URL.
     */
    public static void main(String... args) {
        if (args.length < 1) {
            System.err.println("Usage: ShariffBackend <url> ...");
            return;
        }


        for (String url : args) {
            System.out.println(url);
            try {
                ShariffBackend backend = new ShariffBackend();
                Map<String, Integer> result = backend.getCounts(url);
                for (Map.Entry<String, Integer> entry : result.entrySet()) {
                    System.out.println(String.format("  %-12s: %d", entry.getKey(), entry.getValue()));
                }
            } catch (IOException ex) {
                System.err.println("  Failed: " + ex.getMessage());
            }
            System.out.println();
        }
    }

}
