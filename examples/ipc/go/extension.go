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

/*
 * This is AI generated code
 */

package main

import (
	"bufio"
	"encoding/base64"
	"fmt"
	"os"
	"strings"
)

func main() {
    fmt.Println("This is an example Go extension")

	// Get the input and output file paths from environment variables
	ipcInFile := os.Getenv("PIPELINER_IPC_IN")
	ipcOutFile := os.Getenv("PIPELINER_IPC_OUT")

	// Validate input file
	if ipcInFile == "" {
		fmt.Fprintln(os.Stderr, "Error: PIPELINER_IPC_IN is not set or the file does not exist.")
		os.Exit(1)
	}

	if _, err := os.Stat(ipcInFile); os.IsNotExist(err) {
		fmt.Fprintln(os.Stderr, "Error: PIPELINER_IPC_IN file does not exist.")
		os.Exit(1)
	}

	// Validate output file
	if ipcOutFile == "" {
		fmt.Fprintln(os.Stderr, "Error: PIPELINER_IPC_OUT is not set or the file does not exist.")
		os.Exit(1)
	}

	if _, err := os.Stat(ipcOutFile); os.IsNotExist(err) {
		fmt.Fprintln(os.Stderr, "Error: PIPELINER_IPC_OUT file does not exist.")
		os.Exit(1)
	}

	fmt.Printf("PIPELINER_IPC_IN file [%s]\n", ipcInFile)

	// Read input file into a map
	ipcInProperties := make(map[string]string)

	file, err := os.Open(ipcInFile)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error reading PIPELINER_IPC_IN file: %s\n", err)
		os.Exit(1)
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
        line = strings.TrimSpace(line)

		// Skip empty lines and that start with '#'
		if line == "" || strings.HasPrefix(line, "#") {
			continue
		}

		// Split the line into name and value
		parts := strings.Split(line, " ")
		encodedName := parts[0]
		encodedValue := ""

		if len(parts) > 1 {
			encodedValue = parts[1]
		}

        // Decode the Base64 name
        decodedBytes, err := base64.StdEncoding.DecodeString(encodedName)
        if err != nil {
            fmt.Fprintf(os.Stderr, "Error decoding variable name from PIPELINER_IPC_IN file: %s\n", err)
            os.Exit(1)
        }

        var name = string(decodedBytes)

		// Decode the Base64 value
		var value string

		if encodedValue != "" {
			decodedBytes, err := base64.StdEncoding.DecodeString(encodedValue)
			if err != nil {
				fmt.Fprintf(os.Stderr, "Error decoding variable value from PIPELINER_IPC_IN file: %s\n", err)
                os.Exit(1)
			}

			value = string(decodedBytes)
		}

		// Add to the map
		ipcInProperties[name] = value
	}

	if err := scanner.Err(); err != nil {
		fmt.Fprintf(os.Stderr, "Error reading lines from PIPELINER_IPC_IN file: %s\n", err)
		os.Exit(1)
	}

	// Debug output for the map
	for key, value := range ipcInProperties {
		fmt.Printf("PIPELINER_IPC_IN variable [%s] = [%s]\n", key, value)
	}

	// Example output properties (replace with actual values)
	ipcOutProperties := map[string]string{
		"go_extension_variable_1": "go extension foo",
		"go_extension_variable_2": "go extension bar",
	}

	fmt.Printf("PIPELINER_IPC_OUT file [%s]\n", ipcOutFile)

	// Write the map to the output file with Base64-encoded values
	outFile, err := os.Create(ipcOutFile)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error creating PIPELINER_IPC_OUT file: %s\n", err)
		os.Exit(1)
	}
	defer outFile.Close()

	writer := bufio.NewWriter(outFile)
	for key, value := range ipcOutProperties {
		if key == "" {
			continue // Skip entries with empty keys
		}

        // Base64 encode the name
        var encodedName = base64.StdEncoding.EncodeToString([]byte(key))

		// Base64 encode the value
		var encodedValue string
		encodedValue  = ""
		if value != "" {
			encodedValue = base64.StdEncoding.EncodeToString([]byte(value))
		}

		// Write the key-value pair to the output file
		line := fmt.Sprintf("%s %s\n", encodedName, encodedValue)
		if _, err := writer.WriteString(line); err != nil {
			fmt.Fprintf(os.Stderr, "Error writing to PIPELINER_IPC_OUT file: %s\n", err)
		}

		fmt.Printf("PIPELINER_IPC_OUT variable [%s] = [%s]\n", key, value)
	}

	if err := writer.Flush(); err != nil {
		fmt.Fprintf(os.Stderr, "Error flushing to PIPELINER_IPC_OUT file: %s\n", err)
	}
}
