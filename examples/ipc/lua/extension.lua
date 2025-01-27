--
-- Copyright (C) 2025-present Pipeliner project authors and contributors
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- This is AI generated code

local lfs = require("lfs") -- LuaFileSystem for file existence checks

-- Function to check if a file exists
local function file_exists(file)
    local attr = lfs.attributes(file)
    return attr ~= nil and attr.mode == "file"
end

-- Base64 encoding table
local base64_chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'

-- Function to encode a value to Base64
local function encode_base64(value)
    -- Return the empty string if the input is empty
    if value == "" then
        return ""
    end

    local encoded = {}
    local padding = ""

    -- Add padding if necessary
    local mod = #value % 3
    if mod == 1 then
        value = value .. "\0\0"
        padding = "=="
    elseif mod == 2 then
        value = value .. "\0"
        padding = "="
    end

    -- Encode every 3 bytes into 4 Base64 characters
    for i = 1, #value, 3 do
        local byte1 = string.byte(value, i)
        local byte2 = string.byte(value, i + 1)
        local byte3 = string.byte(value, i + 2)

        local index1 = math.floor(byte1 / 4)
        local index2 = ((byte1 % 4) * 16) + math.floor(byte2 / 16)
        local index3 = ((byte2 % 16) * 4) + math.floor(byte3 / 64)
        local index4 = byte3 % 64

        table.insert(encoded, base64_chars:sub(index1 + 1, index1 + 1))
        table.insert(encoded, base64_chars:sub(index2 + 1, index2 + 1))
        table.insert(encoded, base64_chars:sub(index3 + 1, index3 + 1))
        table.insert(encoded, base64_chars:sub(index4 + 1, index4 + 1))
    end

    -- Replace the last characters with padding if necessary
    local result = table.concat(encoded)
    return result:sub(1, #result - #padding) .. padding
end

local decode_table = {}

-- Helper function to trim whitespace from a string
local function trim(s)
    return s:match("^%s*(.-)%s*$")
end

-- Create a reverse lookup table for decoding
for i = 1, #base64_chars do
    decode_table[base64_chars:sub(i, i)] = i - 1
end

-- Function to decode a Base64 encoded value
local function decode_base64(encoded_value)
    -- Return the empty string if the input is empty
    if encoded_value == "" then
        return ""
    end

    -- Remove padding (if any) and calculate padding length
    local padding = 0
    local len = #encoded_value
    if encoded_value:sub(len - 1, len) == "==" then
        padding = 2
        encoded_value = encoded_value:sub(1, len - 2)
    elseif encoded_value:sub(len, len) == "=" then
        padding = 1
        encoded_value = encoded_value:sub(1, len - 1)
    end

    local decoded = {}
    local i = 1

    -- Decode in chunks of 4 characters
    while i <= #encoded_value do
        -- Get four base64 characters and decode to byte values
        local b1 = decode_table[encoded_value:sub(i, i)]
        local b2 = decode_table[encoded_value:sub(i + 1, i + 1)]
        local b3 = decode_table[encoded_value:sub(i + 2, i + 2)] or 0
        local b4 = decode_table[encoded_value:sub(i + 3, i + 3)] or 0

        -- Combine the four decoded base64 characters into three original bytes
        local byte1 = (b1 * 4) + math.floor(b2 / 16)
        local byte2 = ((b2 % 16) * 16) + math.floor(b3 / 4)
        local byte3 = ((b3 % 4) * 64) + b4

        -- Insert the bytes into the decoded table
        table.insert(decoded, string.char(byte1))
        if i + 2 <= #encoded_value then
            table.insert(decoded, string.char(byte2))
        end
        if i + 3 <= #encoded_value then
            table.insert(decoded, string.char(byte3))
        end

        i = i + 4
    end

    -- Handle padding by removing extra bytes
    if padding == 1 then
        table.remove(decoded)  -- Remove 1 byte for padding
    elseif padding == 2 then
        table.remove(decoded)  -- Remove 2 bytes for padding
        table.remove(decoded)
    end

    return table.concat(decoded)
end

-- Check if the input file is specified and exists
local PIPELINER_IPC_IN = os.getenv("PIPELINER_IPC_IN")
if not PIPELINER_IPC_IN or not file_exists(PIPELINER_IPC_IN) then
    print("Error: PIPELINER_IPC_IN is not set or the file does not exist.")
    os.exit(1)
end

print("PIPELINER_IPC_IN file [" .. PIPELINER_IPC_IN .. "]")

-- Table to store properties
local ipc_in_properties = {}

-- Read the file line by line
for line in io.lines(PIPELINER_IPC_IN) do
    -- Trim the line
    line = trim(line)

    -- Skip empty lines and lines that start with "#"
    if line ~= "" and line:sub(1, 1) ~= "#" then
        -- Split the line based on space
        local encoded_name, encoded_value = line:match("^(%S+)%s*(.*)$")
        if encoded_name then
            -- Base64 decode the name and value
            local name = decode_base64(encoded_name)
            local value = encoded_value ~= "" and decode_base64(encoded_value) or ""

            -- Add to the properties table
            ipc_in_properties[name] = value
        end
    end
end


-- Output the table for debugging or demonstration
for key, value in pairs(ipc_in_properties) do
    print("PIPELINER_IPC_IN property [" .. key .. "] = [" .. value .. "]")
end

print("This is a sample Lua extension")

-- Check if the output file is specified
local PIPELINER_IPC_OUT = os.getenv("PIPELINER_IPC_OUT")
if not PIPELINER_IPC_OUT or not file_exists(PIPELINER_IPC_OUT) then
    print("Error: PIPELINER_IPC_OUT is not set.")
    os.exit(1)
end

print("PIPELINER_IPC_OUT file [" .. PIPELINER_IPC_OUT .. "]")

-- Example table for output properties
local ipc_out_properties = {
    ["extension_property_1"] = "lua.extension.foo",
    ["extension_property_2"] = "lua.extension.bar",
}

-- Write the table to the output file with Base64-encoded values
local output_file = io.open(PIPELINER_IPC_OUT, "a")
if not output_file then
    print("Error: Unable to open PIPELINER_IPC_OUT for writing.")
    os.exit(1)
end

for name, value in pairs(ipc_out_properties) do
    print("PIPELINER_IPC_OUT property [" .. name .. "] = [" .. value .. "]")
    local encoded_name = encode_base64(name)
    local encoded_value = encode_base64(value)
    output_file:write(encoded_name .. " " .. encoded_value .. "\n")
end

output_file:close()
