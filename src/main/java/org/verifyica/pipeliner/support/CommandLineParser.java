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

import java.util.*;

/**
 * CommandLineParser to parse command line arguments.
 */
public class CommandLineParser {

    private final Set<String> flags;
    private final Map<String, List<String>> options;
    private final List<String> arguments;

    private final Set<String> knownFlags;
    private final Set<String> knownOptions;

    /**
     * Constructor
     *
     * @param knownFlags a set of known flags
     * @param knownOptions a set of known options
     */
    public CommandLineParser(Set<String> knownFlags, Set<String> knownOptions) {
        this.knownFlags = knownFlags != null ? knownFlags : Collections.emptySet();
        this.knownOptions = knownOptions != null ? knownOptions : Collections.emptySet();

        this.flags = new HashSet<>();
        this.options = new HashMap<>();
        this.arguments = new ArrayList<>();
    }

    /**
     * Parses the command line arguments.
     *
     * @param arguments the command line arguments
     */
    public void parse(String[] arguments) {
        if (arguments == null) {
            return;
        }

        List<String> argumentList = Arrays.asList(arguments);
        Iterator<String> iterator = argumentList.iterator();
        PeekIterator<String> peekIterator = new PeekIterator<>(iterator);

        while (peekIterator.hasNext()) {
            String argument = peekIterator.next();

            if (argument.startsWith("--")) {
                String name = argument.substring(2);

                if (knownFlags.contains("--" + name)) {
                    flags.add("--" + name);
                } else if (knownOptions.contains("--" + name)) {
                    List<String> optionValues = options.computeIfAbsent("--" + name, k -> new ArrayList<>());
                    while (peekIterator.hasNext() && !peekIterator.peek().startsWith("-")) {
                        optionValues.add(peekIterator.next());
                    }
                } else {
                    throw new IllegalArgumentException("unknown option [--" + name + "]");
                }

            } else if (argument.startsWith("-")) {
                String name = argument.substring(1);

                if (knownFlags.contains("-" + name)) {
                    flags.add("-" + name);
                } else if (knownOptions.contains("-" + name)) {
                    List<String> optionValues = options.computeIfAbsent("-" + name, k -> new ArrayList<>());
                    while (peekIterator.hasNext() && !peekIterator.peek().startsWith("-")) {
                        optionValues.add(peekIterator.next());
                    }
                } else {
                    throw new IllegalArgumentException("unknown option [-" + name + "]");
                }

            } else {
                this.arguments.add(argument);
            }
        }
    }

    /**
     * Returns true if the command line has a flag with the given name.
     *
     * @param name the name of the flag (including - or --)
     * @return true if the flag is present, false otherwise
     */
    public boolean hasFlag(String name) {
        return flags.contains(name);
    }

    /**
     * Returns the values for the option with the given name.
     *
     * @param name the name of the option (including - or --)
     * @return a list of values for the option, or an empty list if the option is not present
     */
    public List<String> getOptionValues(String name) {
        return options.getOrDefault(name, Collections.emptyList());
    }

    /**
     * Returns a set of flags.
     *
     * @return a set of flags
     */
    public Set<String> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    /**
     * Returns a map of options.
     *
     * @return a map of options
     */
    public Map<String, List<String>> getOptions() {
        return Collections.unmodifiableMap(options);
    }

    /**
     * Returns a list of arguments.
     *
     * @return a list of arguments
     */
    public List<String> getArguments() {
        return Collections.unmodifiableList(arguments);
    }
}
