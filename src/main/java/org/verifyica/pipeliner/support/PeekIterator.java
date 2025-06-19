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

package org.verifyica.pipeliner.support;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * PeekIterator provides the ability to peek at the next element in an Iterator without consuming it.
 *
 * @param <T> the type of elements returned by this iterator
 */
public class PeekIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;
    private boolean hasPeeked;
    private T peekedElement;
    private boolean canRemove;

    /**
     * Constructor
     *
     * @param iterator the underlying iterator to peek
     */
    public PeekIterator(Iterator<T> iterator) {
        if (iterator == null) {
            throw new IllegalArgumentException("iterator is null");
        }
        this.iterator = iterator;
    }

    /**
     * Peeks at the next element in the iteration without consuming it.
     *
     * @return the next element
     */
    public T peek() {
        if (!hasPeeked) {
            if (!iterator.hasNext()) {
                throw new NoSuchElementException();
            }
            peekedElement = iterator.next();
            hasPeeked = true;
        }

        canRemove = false; // peek does not allow remove
        return peekedElement;
    }

    /**
     * Returns true if there are more elements to iterate over, false otherwise.
     *
     * @return true if there are more elements, false otherwise
     */
    @Override
    public boolean hasNext() {
        return hasPeeked || iterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element
     * @throws NoSuchElementException if there are no more elements
     */
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        T result;
        if (hasPeeked) {
            result = peekedElement;
            hasPeeked = false;
            peekedElement = null;
        } else {
            result = iterator.next();
        }

        canRemove = true;
        return result;
    }

    /**
     * Removes the last element returned by this iterator.
     * This method can be called only once per call to {@code next()}.
     *
     * @throws IllegalStateException if {@code next()} has not been called, or {@code remove()} has already been called after the last {@code next()}
     */
    @Override
    public void remove() {
        if (!canRemove) {
            throw new IllegalStateException("remove() can only be called once per call to next()");
        }

        iterator.remove();
        canRemove = false;
    }
}
