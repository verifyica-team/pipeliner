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
use std::fmt;
use std::fs::{File, OpenOptions};
use std::io::{self, BufRead, Write};
use std::path::Path;
use std::process;
use std::error::Error;

#[derive(Debug)]
struct IpcException {
    message: String,
    cause: Option<Box<dyn Error>>,
}

impl IpcException {
    fn new(message: &str, cause: Option<Box<dyn Error>>) -> Self {
        IpcException {
            message: message.to_string(),
            cause,
        }
    }
}

// Implement Display for IpcException
impl fmt::Display for IpcException {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        // If there's a cause, display it, otherwise show "None"
        let cause = match &self.cause {
            Some(e) => e.to_string(),
            None => "None".to_string(),
        };
        write!(f, "{}: {}", self.message, cause)
    }
}

// Implement Error for IpcException
impl Error for IpcException {}

#[derive(Debug)]
struct Ipc;

impl Ipc {
    const BASE64_ALPHABET: &[u8; 64] = b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    const BASE64_PADDING: u8 = b'=';

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
                encoded.push(Self::BASE64_ALPHABET[index as usize] as char);
            }
        }

        if bits_collected > 0 {
            buffer <<= 6 - bits_collected;
            let index = buffer & 0b111111;
            encoded.push(Self::BASE64_ALPHABET[index as usize] as char);
        }

        while encoded.len() % 4 != 0 {
            encoded.push('=');
        }

        encoded
    }

    pub fn base64_decode(input: &str) -> Result<String, &'static str> {
        let mut buffer = 0u32;
        let mut bits_collected = 0;
        let mut decoded = Vec::new();

        for &byte in input.as_bytes() {
            if byte == Self::BASE64_PADDING {
                break;
            }

            let value = match Self::BASE64_ALPHABET.iter().position(|&c| c == byte) {
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

    fn read(ipc_file_path: &str) -> Result<HashMap<String, String>, IpcException> {
        let path = Path::new(ipc_file_path);
        let file = File::open(path).map_err(|e| IpcException::new("Failed to open IPC file", Some(Box::new(e))))?;
        let reader = io::BufReader::new(file);

        let mut map = HashMap::new();
        for line in reader.lines() {
            let line = line.map_err(|e| IpcException::new("Failed to read line from IPC file", Some(Box::new(e))))?;
            if !line.trim().is_empty() && !line.starts_with('#') {
                let mut parts = line.splitn(2, '=');
                if let Some(key) = parts.next() {
                    let value = parts.next().map(|v| v.trim()).unwrap_or("");
                    let decoded_value = if value.is_empty() {
                        String::new()
                    } else {
                        match Self::base64_decode(value) {
                            Ok(decoded) => decoded,
                            Err(_) => String::new(),
                        }
                    };
                    map.insert(key.trim().to_string(), decoded_value);
                }
            }
        }
        Ok(map)
    }

    fn write(ipc_file_path: &str, map: &HashMap<String, String>) -> Result<(), IpcException> {
        let path = Path::new(ipc_file_path);
        let mut file = OpenOptions::new()
            .write(true)
            .create(true)
            .truncate(true)
            .open(path)
            .map_err(|e| IpcException::new("Failed to open IPC file for writing", Some(Box::new(e))))?;

        for (key, value) in map {
            let encoded_value = Self::base64_encode(value);
            writeln!(file, "{}={}", key, encoded_value)
                .map_err(|e| IpcException::new("Failed to write to IPC file", Some(Box::new(e))))?;
        }
        Ok(())
    }
}

#[derive(Debug)]
struct Extension;

impl Extension {
    const PIPELINER_TRACE: &'static str = "PIPELINER_TRACE";
    const PIPELINER_IPC_IN: &'static str = "PIPELINER_IPC_IN";
    const PIPELINER_IPC_OUT: &'static str = "PIPELINER_IPC_OUT";

    fn get_environment_variables() -> HashMap<String, String> {
        env::vars().collect()
    }

    fn is_trace_enabled() -> bool {
        env::var(Extension::PIPELINER_TRACE).unwrap_or_default() == "true"
    }

    fn read_ipc_in_properties() -> Result<HashMap<String, String>, IpcException> {
        let ipc_filename_input = env::var(Extension::PIPELINER_IPC_IN)
            .map_err(|e| IpcException::new("Failed to get IPC input file path", Some(Box::new(e))))?;
        let ipc_input_file = Path::new(&ipc_filename_input);
        Ipc::read(ipc_input_file.to_str().unwrap())
    }

    fn write_ipc_out_properties(properties: &HashMap<String, String>) -> Result<(), IpcException> {
        let ipc_filename_output = env::var(Extension::PIPELINER_IPC_OUT)
            .map_err(|e| IpcException::new("Failed to get IPC output file path", Some(Box::new(e))))?;
        let ipc_output_file = Path::new(&ipc_filename_output);
        Ipc::write(ipc_output_file.to_str().unwrap(), properties)
    }

    fn run() -> Result<(), Box<dyn Error>> {
        let environment_variables = Extension::get_environment_variables();
        let ipc_in_properties = Extension::read_ipc_in_properties()?;

        if Extension::is_trace_enabled() {
            for (key, value) in &environment_variables {
                println!("@trace environment variable [{}] = [{}]", key, value);
            }
            for (key, value) in &ipc_in_properties {
                println!("@trace extension property [{}] = [{}]", key, value);
            }
        }

        for (key, value) in &ipc_in_properties {
            println!("PIPELINER_IPC_IN property [{}] = [{}]", key, value);
        }

        println!("This is a sample Rust extension");

        let mut ipc_out_properties = HashMap::new();
        ipc_out_properties.insert("extension.property.1".to_string(), "rust.extension.foo".to_string());
        ipc_out_properties.insert("extension.property.2".to_string(), "rust.extension.bar".to_string());

        for (key, value) in &ipc_out_properties {
            println!("PIPELINER_IPC_OUT property [{}] = [{}]", key, value);
        }

        Extension::write_ipc_out_properties(&ipc_out_properties)?;
        Ok(())
    }

    fn main() -> Result<(), Box<dyn Error>> {
        Extension::run()
    }
}

fn main() {
    if let Err(err) = Extension::main() {
        eprintln!("Error occurred during execution: {}", err);
        process::exit(1);
    }
}
