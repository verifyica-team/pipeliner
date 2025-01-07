/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

import java.util.Iterator;

/**
 * Class to implement PeekIterator
 *
 * @param <T> type of the iterator
 */
public class PeekIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;
    private T nextElement;
    private boolean hasNext;

    /**
     * Constructor
     *
     * @param iterable iterable
     */
    public PeekIterator(Iterable<T> iterable) {
        this(iterable.iterator());
    }

    /**
     * Constructor
     *
     * @param iterator iterator
     */
    public PeekIterator(Iterator<T> iterator) {
        this.iterator = iterator;

        // Advance the iterator
        advance();
    }

    /**
     * Method to peek the next element
     *
     * @return next element
     * @throws IllegalStateException if no next element available to peek
     */
    public T peek() {
        if (!hasNext) {
            throw new IllegalStateException("No next element available to peek");
        }

        return nextElement;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public T next() {
        if (!hasNext) {
            throw new IllegalStateException("No next element");
        }

        // Get the next element
        T current = nextElement;

        // Advance the iterator
        advance();

        return current;
    }

    /**
     * Method to advance the iterator
     */
    private void advance() {
        if (iterator.hasNext()) {
            // Advance the iterator
            nextElement = iterator.next();
            hasNext = true;
        } else {
            // No next element
            nextElement = null;
            hasNext = false;
        }
    }
}
