/*
 * Copyright (C) Pipeliner project authors and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.verifyica.pipeliner.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class to implement a Least Recently Used (LRU) cache.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class LRUCache<K, V> {

    private final Map<K, V> cache;

    /**
     * Creates an LRU cache with a specified maximum number of elements
     *
     * @param maximumEntries the maximum number of entries in the cache
     */
    public LRUCache(final int maximumEntries) {
        this.cache = new LinkedHashMap<K, V>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maximumEntries;
            }
        };
    }

    /**
     * Adds an entry to the cache. Re-ages the entry if it already exists
     *
     * @param key the key
     * @param value the value
     */
    public synchronized void put(K key, V value) {
        cache.put(key, value);
    }

    /**
     * Retrieves a value from the cache.
     *
     * @param key the key
     * @return the value associated with the key, or null if the key is not in the cache
     */
    public synchronized V get(K key) {
        return cache.get(key);
    }

    /**
     * Removes an entry from the cache
     *
     * @param key the key
     * @return the previous value associated with the key, or null if there was no mapping
     */
    public synchronized V remove(K key) {
        return cache.remove(key);
    }

    /**
     * Returns the current number of entries in the cache
     *
     * @return the number of elements in the cache
     */
    public synchronized int size() {
        return cache.size();
    }

    /**
     * Removes all entries in the cache
     */
    public synchronized void clear() {
        cache.clear();
    }
}
