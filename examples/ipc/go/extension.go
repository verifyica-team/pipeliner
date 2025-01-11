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

/*
 * This is AI generated code
 */

package main

import (
	"bufio"
	"encoding/base64"
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"strings"
)

const (
	pipelinerTrace  = "PIPELINER_TRACE"
	pipelinerIpcIn  = "PIPELINER_IPC_IN"
	pipelinerIpcOut = "PIPELINER_IPC_OUT"
)

// IpcException represents a custom error with additional context.
type IpcException struct {
	Message string
	Cause   error
}

func (e *IpcException) Error() string {
	if e.Cause != nil {
		return fmt.Sprintf("%s: %v", e.Message, e.Cause)
	}
	return e.Message
}

// Ipc provides utility methods for inter-process communication.
type Ipc struct{}

// Read reads properties from an IPC file.
func (Ipc) Read(ipcFilePath string) (map[string]string, error) {
	// Open the IPC file
	file, err := os.Open(ipcFilePath)
	if err != nil {
		return nil, &IpcException{Message: "Failed to read IPC file", Cause: err}
	}
	defer file.Close()

	// Initialize the map to store properties
	properties := make(map[string]string)
	scanner := bufio.NewScanner(file)

	// Read the file line by line
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())

		// Skip empty lines and comments
		if len(line) > 0 && !strings.HasPrefix(line, "#") {
			parts := strings.SplitN(line, "=", 2)

			// Handle key-value pairs
			if len(parts) == 2 {
				decodedValue, err := base64.StdEncoding.DecodeString(strings.TrimSpace(parts[1]))
				if err != nil {
					return nil, &IpcException{
						Message: fmt.Sprintf("Error decoding Base64 for key '%s'", strings.TrimSpace(parts[0])),
						Cause:   err,
					}
				}
				properties[strings.TrimSpace(parts[0])] = string(decodedValue)
			} else {
				// If no value is present, store an empty string
				properties[strings.TrimSpace(parts[0])] = ""
			}
		}
	}

	// Check for scanning errors
	if err := scanner.Err(); err != nil {
		return nil, &IpcException{Message: "Failed to parse IPC file", Cause: err}
	}

	return properties, nil
}

// Write writes properties to an IPC file.
func (Ipc) Write(ipcFilePath string, data map[string]string) error {
	// Create or overwrite the IPC file
	file, err := os.Create(ipcFilePath)
	if err != nil {
		return &IpcException{Message: "Failed to write IPC file", Cause: err}
	}
	defer file.Close()

	writer := bufio.NewWriter(file)

	// Write each key-value pair
	for key, value := range data {
		var encodedValue string
		if value == "" {
			encodedValue = ""
		} else {
			encodedValue = base64.StdEncoding.EncodeToString([]byte(value))
		}

		// Write the line to the file
		_, err := writer.WriteString(fmt.Sprintf("%s=%s\n", key, encodedValue))
		if err != nil {
			return &IpcException{Message: "Failed to write data to IPC file", Cause: err}
		}
	}

	// Flush the buffer to ensure data is written to the file
	if err := writer.Flush(); err != nil {
		return &IpcException{Message: "Failed to flush data to IPC file", Cause: err}
	}

	return nil
}


// Extension represents the main functionality of the extension.
type Extension struct{}

// Run executes the extension logic.
func (e *Extension) Run(args []string) error {
	envVars := e.GetEnvironmentVariables()

	ipcInProperties, err := e.ReadIpcInProperties()
	if err != nil {
		return err
	}

	if e.IsTraceEnabled() {
		for key, value := range envVars {
			fmt.Printf("@trace environment variable [%s] = [%s]\n", key, value)
		}
		for key, value := range ipcInProperties {
			fmt.Printf("@trace extension property [%s] = [%s]\n", key, value)
		}
	}

	for key, value := range ipcInProperties {
		fmt.Printf("PIPELINER_IPC_IN property [%s] = [%s]\n", key, value)
	}

	fmt.Println("This is a sample Go extension")

	ipcOutProperties := map[string]string{
		"extension.property.1": "go.extension.foo",
		"extension.property.2": "go.extension.bar",
	}

    fmt.Printf("PIPELINER_IPC_OUT file [%s]\n", os.Getenv(pipelinerIpcOut))

	for key, value := range ipcOutProperties {
		fmt.Printf("PIPELINER_IPC_OUT property [%s] = [%s]\n", key, value)
	}

	return e.WriteIpcOutProperties(ipcOutProperties)
}

// ReadIpcInProperties reads properties from the input IPC file.
func (e *Extension) ReadIpcInProperties() (map[string]string, error) {
	ipcFilePath := os.Getenv(pipelinerIpcIn)
	fmt.Printf("%s file [%s]\n", pipelinerIpcIn, ipcFilePath)

	ipcPath, err := filepath.Abs(ipcFilePath)
	if err != nil {
		return nil, errors.New("failed to resolve IPC input file path")
	}

	return Ipc{}.Read(ipcPath)
}

// WriteIpcOutProperties writes properties to the output IPC file.
func (e *Extension) WriteIpcOutProperties(properties map[string]string) error {
	ipcFilePath := os.Getenv(pipelinerIpcOut)
	ipcPath, err := filepath.Abs(ipcFilePath)
	if err != nil {
		return errors.New("failed to resolve IPC output file path")
	}

	return Ipc{}.Write(ipcPath, properties)
}

// GetEnvironmentVariables retrieves all environment variables as a map.
func (e *Extension) GetEnvironmentVariables() map[string]string {
	envVars := make(map[string]string)
	for _, env := range os.Environ() {
		parts := strings.SplitN(env, "=", 2)
		if len(parts) == 2 {
			envVars[parts[0]] = parts[1]
		}
	}
	return envVars
}

// IsTraceEnabled checks if trace is enabled.
func (e *Extension) IsTraceEnabled() bool {
	return os.Getenv(pipelinerTrace) == "true"
}

// Main entry point for the extension.
func main() {
	args := os.Args[1:]

	extension := &Extension{}
	if err := extension.Run(args); err != nil {
		fmt.Fprintf(os.Stderr, "Error occurred during execution: %v\n", err)
		os.Exit(1)
	}
}

