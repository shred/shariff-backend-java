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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A very simple cache. It keeps a maximum number of elements for the given time to live.
 * If the maximum number is reached, the least recently read element is removed. Timed out
 * elements are not purged automatically.
 * <p>
 * This cache is not thread safe!
 *
 * @param <K>
 *            Key type
 * @param <V>
 *            Value type
 * @author Richard "Shred" Körber
 */
public class SimpleCache<K, V> {

    private final CacheMap<K, Entry<V>> cache;
    private final long timeToLiveMs;

    /**
     * Creates a new {@link SimpleCache}.
     *
     * @param maxEntries
     *            maximum number of elements to keep
     * @param timeToLive
     *            maximum time to live for each element
     * @param unit
     *            {@link TimeUnit} of timeToLive
     */
    public SimpleCache(int maxEntries, long timeToLive, TimeUnit unit) {
        cache = new CacheMap<>(maxEntries);
        this.timeToLiveMs = unit.toMillis(timeToLive);
    }

    /**
     * Creates a new {@link SimpleCache}.
     *
     * @param maxEntries
     *            maximum number of elements to keep
     * @param timeToLiveMs
     *            maximum time to live for each element
     * @deprecated Use {@link #SimpleCache(int, long, TimeUnit)}
     */
    @Deprecated
    public SimpleCache(int maxEntries, long timeToLiveMs) {
        this(maxEntries, timeToLiveMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets an element from the cache.
     *
     * @param key
     *            Cache key
     * @return Value, or {@code null} if there was no such element or the element was
     *         expired
     */
    public V get(K key) {
        Entry<V> entry = cache.get(key);

        if (entry != null && !isExpired(entry.expiry)) {
            return entry.value;
        } else if (entry != null) {
            cache.remove(key);
        }

        return null;
    }

    /**
     * Puts an element into the cache. If the key was already present in the cache, it is
     * replaced.
     *
     * @param key
     *            Cache key
     * @param value
     *            Cache value
     */
    public void put(K key, V value) {
        cache.put(key, new Entry<>(value, System.currentTimeMillis() + timeToLiveMs));
    }

    /**
     * Returns the current cache size. The size includes entries that have timed out, but
     * have not been purged yet.
     *
     * @return Cache size
     */
    protected int size() {
        return cache.size();
    }

    /**
     * Checks if the expiry date has been reached.
     *
     * @param expiry
     *            Expiry time to check
     * @return {@code true} if the expiry date has been reached
     */
    protected boolean isExpired(long expiry) {
        return expiry < System.currentTimeMillis();
    }

    /**
     * A {@link LinkedHashMap} that keeps the given maximum number of elements.
     */
    private static class CacheMap<T, U> extends LinkedHashMap<T, U> { //NOSONAR: S2160 false positive
        private static final long serialVersionUID = -294328016365489050L;

        private final int maxEntries;

        /**
         * Creates a new {@link CacheMap}.
         *
         * @param maxEntries
         *            maximum number of elements to keep
         */
        public CacheMap(int maxEntries) {
            super(maxEntries / 4, 0.75f, true);
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<T, U> eldest) {
            return super.size() > maxEntries;
        }
    }

    /**
     * A cache entry.
     */
    private static class Entry<V> {
        private final V value;
        private final long expiry;

        /**
         * Creates a new entry.
         *
         * @param value
         *            Value to be cached
         * @param expiry
         *            Expiry time stamp
         */
        public Entry(V value, long expiry) {
            this.value = value;
            this.expiry = expiry;
        }
    }

}
