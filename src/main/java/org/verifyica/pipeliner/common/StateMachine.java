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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to implement StateMachine
 *
 * @param <T> the type
 */
public class StateMachine<T> {

    private final HashSet<T> EMPTY_SET = new HashSet<>();

    private T currentState;
    private final Map<T, Set<T>> transitions;

    /**
     * Constructor
     *
     * <p>null initial state</p>
     */
    public StateMachine() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param initialState the initial state
     */
    public StateMachine(T initialState) {
        this.currentState = initialState;
        transitions = new HashMap<>();
    }

    /**
     * Method to get the current state
     *
     * @return the current state
     */
    public T currentState() {
        return currentState;
    }

    /**
     * Method to add a transition
     *
     * @param from the from state
     * @param to the to state
     * @return this
     */
    public StateMachine<T> addTransition(T from, T to) {
        transitions.getOrDefault(from, new HashSet<>()).add(to);
        return this;
    }

    /**
     * Method to add transitions
     *
     * @param from the from state
     * @param tos the to states
     * @return this
     */
    @SafeVarargs
    public final StateMachine<T> addTransitions(T from, T... tos) {
        transitions.computeIfAbsent(from, s -> new HashSet<>()).addAll(Arrays.asList(tos));
        return this;
    }

    /**
     * Method to transition
     *
     * @param to the to state
     * @return true if the transition is valid and the state machine has transitioned, else false
     */
    public boolean transition(T to) {
        Set<T> toStates = transitions.getOrDefault(currentState, EMPTY_SET);
        boolean isValid = toStates.contains(to);
        if (isValid) {
            currentState = to;
        }
        return isValid;
    }
}
