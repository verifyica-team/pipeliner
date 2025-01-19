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

use std::collections::HashMap;
use std::env;
use std::fs;
use std::io::Write;

const BASE64_ALPHABET: &[u8; 64] = b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
const BASE64_PADDING: u8 = b'=';

// Base64 Encoding
pub fn base64_encode(input: &str) -> String {
    let bytes = input.as_bytes();
    let mut encoded = String::new();
    let mut buffer = 0u32;
    let mut bits_collected = 0;

    for &byte in bytes {
        buffer = (buffer << 8) | byte as u32;
        bits_collected += 8;

        while bits_collected >= 6 {
            bits_collected -= 6;
            let index = (buffer >> bits_collected) & 0b111111;
            encoded.push(BASE64_ALPHABET[index as usize] as char);
        }
    }

    if bits_collected > 0 {
        buffer <<= 6 - bits_collected;
        let index = buffer & 0b111111;
        encoded.push(BASE64_ALPHABET[index as usize] as char);
    }

    while encoded.len() % 4 != 0 {
        encoded.push('=');
    }

    encoded
}

// Base64 Decoding
pub fn base64_decode(input: &str) -> Result<String, &'static str> {
    let mut buffer = 0u32;
    let mut bits_collected = 0;
    let mut decoded = Vec::new();

    for &byte in input.as_bytes() {
        if byte == BASE64_PADDING {
            break;
        }

        let value = match BASE64_ALPHABET.iter().position(|&c| c == byte) {
            Some(v) => v as u8,
            None => return Err("Invalid Base64 character"),
        };

        buffer = (buffer << 6) | value as u32;
        bits_collected += 6;

        if bits_collected >= 8 {
            bits_collected -= 8;
            let decoded_byte = (buffer >> bits_collected) & 0xFF;
            decoded.push(decoded_byte as u8);
        }
    }

    String::from_utf8(decoded).map_err(|_| "Invalid UTF-8 sequence in decoded output")
}

// Main Program
pub fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Get the input and output file paths from environment variables
    let ipc_in_file = env::var("PIPELINER_IPC_IN").unwrap_or_default();
    let ipc_out_file = env::var("PIPELINER_IPC_OUT").unwrap_or_default();

    // Validate input file
    if ipc_in_file.is_empty() || !std::path::Path::new(&ipc_in_file).exists() {
        eprintln!("Error: PIPELINER_IPC_IN is not set or the file does not exist.");
        std::process::exit(1);
    }

    // Validate output file
    if ipc_out_file.is_empty() || !std::path::Path::new(&ipc_out_file).exists() {
        eprintln!("Error: PIPELINER_IPC_OUT is not set or the file does not exist.");
        std::process::exit(1);
    }

    println!("PIPELINER_IPC_IN file [{}]", ipc_in_file);

    // Read input file into a HashMap
    let mut ipc_in_properties = HashMap::new();
    let contents = fs::read_to_string(&ipc_in_file)?;

    for line in contents.lines() {
        // Skip empty lines and lines without '='
        if line.trim().is_empty() || !line.contains('=') {
            continue;
        }

        // Split the line into key and value
        let mut parts = line.splitn(2, '=');
        let key = parts.next().unwrap_or("").trim();
        let encoded_value = parts.next().unwrap_or("").trim();

        // Decode the Base64 value
        let decoded_value = if encoded_value.is_empty() {
            String::new()
        } else {
            match base64_decode(encoded_value) {
                Ok(value) => value,
                Err(err) => {
                    eprintln!("Error decoding Base64 for key [{}]: {}", key, err);
                    continue;
                }
            }
        };

        ipc_in_properties.insert(key.to_string(), decoded_value);
    }

    // Debug output for the HashMap
    for (key, value) in &ipc_in_properties {
        println!("PIPELINER_IPC_IN property [{}] = [{}]", key, value);
    }

    println!("This is a sample Rust extension");

    // A property name must match the regular expression `^[a-zA-Z0-9_][a-zA-Z0-9_.-]*[a-zA-Z0-9_]$`

    // Example output properties (replace with actual values)
    let ipc_out_properties: HashMap<&str, &str> = HashMap::from([
        ("extension.property.1", "rust.extension.foo"),
        ("extension.property.2", "rust.extension.bar"),
    ]);

    println!("PIPELINER_IPC_OUT file [{}]", ipc_out_file);

    // Write the HashMap to the output file with Base64-encoded values
    let mut file = fs::File::create(&ipc_out_file)?;

    for (key, value) in &ipc_out_properties {
        if key.is_empty() {
            continue; // Skip entries with empty keys
        }

        println!("PIPELINER_IPC_OUT property [{}] = [{}]", key, value);

        // Base64 encode the value
        let encoded_value = if value.is_empty() {
            String::new()
        } else {
            base64_encode(value)
        };

        // Write the key-value pair to the output file
        writeln!(file, "{}={}", key, encoded_value)?;
    }

    Ok(())
}
