package main

import (
	"bufio"
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"strings"
)

const (
	bufferSizeBytes            = 16384
	temporaryDirectoryPrefix   = "pipeliner-ipc-"
	temporaryDirectorySuffix   = ""
	pipelinerTrace             = "PIPELINER_TRACE"
	pipelinerIpcIn             = "PIPELINER_IPC_IN"
	pipelinerIpcOut            = "PIPELINER_IPC_OUT"
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
	file, err := os.Open(ipcFilePath)
	if err != nil {
		return nil, &IpcException{Message: "Failed to read IPC file", Cause: err}
	}
	defer file.Close()

	properties := make(map[string]string)
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if len(line) > 0 && !strings.HasPrefix(line, "#") {
			parts := strings.SplitN(line, "=", 2)
			if len(parts) == 2 {
				properties[strings.TrimSpace(parts[0])] = unescapeCRLF(parts[1])
			}
		}
	}
	if err := scanner.Err(); err != nil {
		return nil, &IpcException{Message: "Failed to parse IPC file", Cause: err}
	}

	return properties, nil
}

// Write writes properties to an IPC file.
func (Ipc) Write(ipcFilePath string, data map[string]string) error {
	file, err := os.Create(ipcFilePath)
	if err != nil {
		return &IpcException{Message: "Failed to write IPC file", Cause: err}
	}
	defer file.Close()

	writer := bufio.NewWriter(file)
	for key, value := range data {
		_, _ = writer.WriteString(fmt.Sprintf("%s=%s\n", key, escapeCRLF(value)))
	}
	writer.Flush()

	return nil
}

// Function to escape \, \r, and \n
func escapeCRLF(value string) string {
    value = strings.ReplaceAll(value, `\`, `\\`)
    value = strings.ReplaceAll(value, `\r`, `\\r`)
    value = strings.ReplaceAll(value, `\n`, `\\n`)

    return value
}

// Function to unescape \\, \\r, and \\n
func unescapeCRLF(value string) string {
    value = strings.ReplaceAll(value, `\\n`, `\n`)
    value = strings.ReplaceAll(value, `\\r`, `\r`)
    value = strings.ReplaceAll(value, `\\`, `\`)

    return value
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

	fmt.Println("This is a sample Go extension")
	for key, value := range ipcInProperties {
		fmt.Printf("extension with property [%s] = [%s]\n", key, value)
	}

	ipcOutProperties := map[string]string{
		"extension.property.1": "extension.foo",
		"extension.property.2": "extension.bar",
	}

	return e.WriteIpcOutProperties(ipcOutProperties)
}

// ReadIpcInProperties reads properties from the input IPC file.
func (e *Extension) ReadIpcInProperties() (map[string]string, error) {
	ipcFilePath := os.Getenv(pipelinerIpcIn)
	fmt.Printf("%s [%s]\n", pipelinerIpcIn, ipcFilePath)

	ipcPath, err := filepath.Abs(ipcFilePath)
	if err != nil {
		return nil, errors.New("failed to resolve IPC input file path")
	}

	return Ipc{}.Read(ipcPath)
}

// WriteIpcOutProperties writes properties to the output IPC file.
func (e *Extension) WriteIpcOutProperties(properties map[string]string) error {
	ipcFilePath := os.Getenv(pipelinerIpcOut)
	fmt.Printf("%s [%s]\n", pipelinerIpcOut, ipcFilePath)

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

