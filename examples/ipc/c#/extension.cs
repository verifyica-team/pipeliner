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
        var ipcInProperties = new Dictionary<string, string>();

        foreach (var line in File.ReadLines(ipcInFile))
        {
            // Trim the line
            var trimmedLine = line?.Trim();

            // Skip empty lines and comments
            if (string.IsNullOrEmpty(trimmedLine) || trimmedLine.StartsWith("#")) {
                continue;
            }

            // Split the line into name and value
            var nameValue = trimmedLine.Split(' ');

            string encodedName = nameValue[0];
            string encodedValue = nameValue.Length > 1 ? nameValue[1] : string.Empty;

            // Decode the Base64 name
            string name = Encoding.UTF8.GetString(Convert.FromBase64String(encodedName));

            // Decode the Base64 value
            string value = string.IsNullOrEmpty(encodedValue)
                ? string.Empty
                : Encoding.UTF8.GetString(Convert.FromBase64String(encodedValue));

            // Add to the dictionary
            ipcInProperties[name] = value;
        }

        // Debug output for the dictionary
        foreach (var nameValue in ipcInProperties)
        {
            Console.WriteLine($"PIPELINER_IPC_IN variable [{nameValue.Key}] = [{nameValue.Value}]");
        }

        Console.WriteLine("This is a sample C# extension");

        // Validate output file
        if (string.IsNullOrEmpty(ipcOutFile))
        {
            Console.Error.WriteLine("Error: PIPELINER_IPC_OUT is not set.");
            Environment.Exit(1);
        }

        Console.WriteLine($"PIPELINER_IPC_OUT file [{ipcOutFile}]");

        // Example output properties (replace with actual values)
        var ipcOutProperties = new Dictionary<string, string>
        {
            { "csharp_extension_variable_1", "c# extension foo" },
            { "csharp_extension_variable_2", "c# extension bar" }
        };

        // Write the dictionary to the output file with Base64-encoded values
        using (var writer = new StreamWriter(ipcOutFile, false, new UTF8Encoding(false)))
        {
            foreach (var nameValue in ipcOutProperties)
            {
                if (string.IsNullOrEmpty(nameValue.Key))
                {
                    continue; // Skip entries with null keys
                }

                try
                {
                    Console.WriteLine($"PIPELINER_IPC_OUT variable [{nameValue.Key}] = [{nameValue.Value}]");

                    // Base64 encode the key
                    string encodedName = Convert.ToBase64String(Encoding.UTF8.GetBytes(nameValue.Key));

                    // Base64 encode the value
                    string encodedValue = string.IsNullOrEmpty(nameValue.Value) ? string.Empty : Convert.ToBase64String(Encoding.UTF8.GetBytes(nameValue.Value));

                    // Write the key-value pair to the output file
                    writer.WriteLine($"{encodedName} {encodedValue}");
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Error processing variable [{nameValue.Key}]: {ex.Message}");
                }
            }
        }
    }
}