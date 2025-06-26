--
-- Copyright (C) Pipeliner project authors and contributors
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
local base64 = require("base64") -- Lua Base64 library

-- Function to check if a file exists
local function file_exists(file)
    local attr = lfs.attributes(file)
    return attr ~= nil and attr.mode == "file"
end

-- Helper function to trim whitespace from a string
local function trim(s)
    return s:match("^%s*(.*)$")
end

print("This is a sample Lua extension")

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
        local encoded_name, encoded_value = line:match("^(.-)%s+(.*)$")
        if encoded_name then
            -- Base64 decode the name and value
            local name = base64.decode(encoded_name)
            local value = encoded_value ~= "" and base64.decode(encoded_value) or ""

            -- Add to the properties table
            ipc_in_properties[name] = value
        end
    end
end


-- Output the table for debugging or demonstration
for key, value in pairs(ipc_in_properties) do
    print("PIPELINER_IPC_IN variable [" .. key .. "] = [" .. value .. "]")
end


-- Check if the output file is specified
local PIPELINER_IPC_OUT = os.getenv("PIPELINER_IPC_OUT")
if not PIPELINER_IPC_OUT or not file_exists(PIPELINER_IPC_OUT) then
    print("Error: PIPELINER_IPC_OUT is not set.")
    os.exit(1)
end

print("PIPELINER_IPC_OUT file [" .. PIPELINER_IPC_OUT .. "]")

-- Example table for output properties
local ipc_out_properties = {
    ["lua_extension_variable_1"] = "lua extension foo",
    ["lua_extension_variable_2"] = "lua extension bar",
}

-- Write the table to the output file with Base64-encoded values
local output_file = io.open(PIPELINER_IPC_OUT, "a")
if not output_file then
    print("Error: Unable to open PIPELINER_IPC_OUT for writing.")
    os.exit(1)
end

for name, value in pairs(ipc_out_properties) do
    print("PIPELINER_IPC_OUT variable [" .. name .. "] = [" .. value .. "]")
    local encoded_name = base64.encode(name)
    local encoded_value = base64.encode(value)
    output_file:write(encoded_name .. " " .. encoded_value .. "\n")
end

output_file:close()
