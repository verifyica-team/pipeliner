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

using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

static class Extension
{
    static void Main(string[] args)
    {
        // Get the input and output file paths from environment variables
        string ipcInFile = Environment.GetEnvironmentVariable("PIPELINER_IPC_IN") ?? string.Empty;
        string ipcOutFile = Environment.GetEnvironmentVariable("PIPELINER_IPC_OUT") ?? string.Empty;

        // Validate input file
        if (string.IsNullOrEmpty(ipcInFile) || !File.Exists(ipcInFile))
        {
            Console.Error.WriteLine("Error: PIPELINER_IPC_IN is not set or the file does not exist.");
            Environment.Exit(1);
        }

        // Validate output file
        if (string.IsNullOrEmpty(ipcOutFile) || !File.Exists(ipcOutFile))
        {
            Console.Error.WriteLine("Error: PIPELINER_IPC_OUT is not set or the file does not exist.");
            Environment.Exit(1);
        }

        Console.WriteLine($"PIPELINER_IPC_IN file [{ipcInFile}]");

        // Read input file into a dictionary
        var ipcInVariables = new Dictionary<string, string>();

        foreach (var line in File.ReadLines(ipcInFile))
        {
            // Skip empty lines and lines without '='
            var trimmedLine = line?.Trim();
            if (string.IsNullOrEmpty(trimmedLine) || trimmedLine.StartsWith("#")) {
                continue;
            }

            // Split the line into key and value
            var keyValue = trimmedLine.Split(new[] { '=' }, 2);
            string key = keyValue[0];
            string encodedValue = keyValue.Length > 1 ? keyValue[1] : string.Empty;

            // Decode the Base64 value
            string decodedValue = string.IsNullOrEmpty(encodedValue)
                ? string.Empty
                : Encoding.UTF8.GetString(Convert.FromBase64String(encodedValue));

            // Add to the dictionary
            ipcInVariables[key] = decodedValue;
        }

        // Debug output for the dictionary
        foreach (var kvp in ipcInVariables)
        {
            Console.WriteLine($"PIPELINER_IPC_IN variable [{kvp.Key}] = [{kvp.Value}]");
        }

        Console.WriteLine("This is a sample C# extension");

        // Validate output file
        if (string.IsNullOrEmpty(ipcOutFile))
        {
            Console.Error.WriteLine("Error: PIPELINER_IPC_OUT is not set.");
            Environment.Exit(1);
        }

        Console.WriteLine($"PIPELINER_IPC_OUT file [{ipcOutFile}]");

        // A variable name must match the regular expression `^[a-zA-Z0-9_][a-zA-Z0-9_-]*[a-zA-Z0-9_]$`

        // Example output properties (replace with actual values)
        var ipcOutVariables = new Dictionary<string, string>
        {
            { "extension_variable_1", "c# extension foo" },
            { "extension_variable_2", "c# extension bar" }
        };

        // Write the dictionary to the output file with Base64-encoded values
        using (var writer = new StreamWriter(ipcOutFile, false, new UTF8Encoding(false)))
        {
            foreach (var kvp in ipcOutVariables)
            {
                if (string.IsNullOrEmpty(kvp.Key))
                {
                    continue; // Skip entries with null keys
                }

                try
                {
                    Console.WriteLine($"PIPELINER_IPC_OUT variable [{kvp.Key}] = [{kvp.Value}]");

                    string encodedValue;

                    if (string.IsNullOrEmpty(kvp.Value))
                    {
                        encodedValue = string.Empty;
                    } else {
                        // Base64 encode the value
                        encodedValue = Convert.ToBase64String(Encoding.UTF8.GetBytes(kvp.Value));
                    }

                    // Write the key-value pair to the output file
                    writer.WriteLine($"{kvp.Key}={encodedValue}");
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Error processing variable [{kvp.Key}]: {ex.Message}");
                }
            }
        }
    }
}