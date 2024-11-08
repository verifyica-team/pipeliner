package org.verifyica.pipeline.common;

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

