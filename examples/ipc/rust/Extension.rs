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
    fn escape_crlf(value: &str) -> String {
        value.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n")
    }

    fn unescape_crlf(value: &str) -> String {
        value.replace("\\n", "\n").replace("\\r", "\r").replace("\\\\", "\\")
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
                if let (Some(key), Some(value)) = (parts.next(), parts.next()) {
                    map.insert(key.trim().to_string(), Ipc::unescape_crlf(value.trim()));
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
            let escaped_value = Ipc::escape_crlf(value);
            writeln!(file, "{}={}", key, escaped_value)
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
        ipc_out_properties.insert("extension.property.1".to_string(), "extension.foo".to_string());
        ipc_out_properties.insert("extension.property.2".to_string(), "extension.bar".to_string());

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
