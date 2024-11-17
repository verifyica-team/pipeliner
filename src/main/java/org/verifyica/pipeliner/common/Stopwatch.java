/*
 * Copyright (C) 2024-present Pipeliner project authors and contributors
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

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Class to implement Stopwatch */
@SuppressWarnings("UnusedReturnValue")
public class Stopwatch {

    private final ReadWriteLock readWriteLock;
    private long startNanoTime;
    private Long stopNanoTime;

    /**
     * Constructor
     *
     * <p>The Stopwatch starts automatically
     */
    public Stopwatch() {
        readWriteLock = new ReentrantReadWriteLock(true);
        reset();
    }

    /**
     * Method to reset the Stopwatch
     *
     * @return the Stopwatch
     */
    public Stopwatch reset() {
        readWriteLock.writeLock().lock();
        try {
            startNanoTime = System.nanoTime();
            stopNanoTime = null;
            return this;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Method to stop the Stopwatch
     *
     * @return the Stopwatch
     */
    public Stopwatch stop() {
        readWriteLock.writeLock().lock();
        try {
            stopNanoTime = System.nanoTime();
            return this;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Method to get the Stopwatch elapsed time in nanoseconds
     *
     * @return the Stopwatch elapsed time in nanoseconds
     */
    public Duration elapsedTime() {
        readWriteLock.readLock().lock();
        try {
            if (stopNanoTime == null) {
                return Duration.ofNanos(System.nanoTime() - startNanoTime);
            } else {
                return Duration.ofNanos(stopNanoTime - startNanoTime);
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        return String.valueOf(elapsedTime().toNanos());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stopwatch stopwatch = (Stopwatch) o;

        readWriteLock.readLock().lock();
        try {
            return startNanoTime == stopwatch.startNanoTime && Objects.equals(stopNanoTime, stopwatch.stopNanoTime);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public int hashCode() {
        readWriteLock.readLock().lock();
        try {
            return Objects.hash(startNanoTime, stopNanoTime);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
