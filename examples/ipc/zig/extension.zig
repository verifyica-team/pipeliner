//
// Copyright (C) Pipeliner project authors and contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

//
// This is AI generated code
//

const std = @import("std");

fn base64Encode(allocator: std.mem.Allocator, input: []const u8) ![]u8 {
    const encoded_len = ((input.len + 2) / 3) * 4;
    const buffer = try allocator.alloc(u8, encoded_len);
    _ = std.base64.standard.Encoder.encode(buffer, input);
    return buffer;
}

fn base64Decode(allocator: std.mem.Allocator, input: []const u8) ![]u8 {
    const max_len = (input.len * 3) / 4;
    const buffer = try allocator.alloc(u8, max_len);
    _ = try std.base64.standard.Decoder.decode(buffer, input);
    // Since decode returns void, we'll manually find the length
    var actual_len: usize = 0;
    while (actual_len < buffer.len and buffer[actual_len] != 0) : (actual_len += 1) {}
    return buffer[0..actual_len];
}

fn isValidUtf8(bytes: []const u8) bool {
    var i: usize = 0;
    while (i < bytes.len) {
        const width = std.unicode.utf8ByteSequenceLength(bytes[i]) catch return false;
        if (i + width > bytes.len) return false;

        _ = std.unicode.utf8Decode(bytes[i .. i + width]) catch return false;
        i += width;
    }
    return true;
}

pub fn main() !void {
    try std.io.getStdOut().writer().print("This is an example Zig extension\n", .{});

    const allocator = std.heap.page_allocator;
    var stdout = std.io.getStdOut().writer();

    const ipc_in_file = std.process.getEnvVarOwned(allocator, "PIPELINER_IPC_IN") catch {
        try stdout.print("Error: PIPELINER_IPC_IN is not set.\n", .{});
        std.process.exit(1);
    };
    defer allocator.free(ipc_in_file);

    const ipc_out_file = std.process.getEnvVarOwned(allocator, "PIPELINER_IPC_OUT") catch {
        try stdout.print("Error: PIPELINER_IPC_OUT is not set.\n", .{});
        std.process.exit(1);
    };
    defer allocator.free(ipc_out_file);

    var in_file = std.fs.cwd().openFile(ipc_in_file, .{}) catch {
        try stdout.print("Error: Unable to open PIPELINER_IPC_IN file: {s}\n", .{ipc_in_file});
        std.process.exit(1);
    };
    defer in_file.close();

    const in_content = try in_file.readToEndAlloc(allocator, 1 << 20);
    defer allocator.free(in_content);

    var ipc_in_properties = std.StringHashMap([]const u8).init(allocator);
    defer {
        var it = ipc_in_properties.iterator();
        while (it.next()) |entry| {
            allocator.free(entry.key_ptr.*);
            allocator.free(entry.value_ptr.*);
        }
        ipc_in_properties.deinit();
    }

    var line_iter = std.mem.tokenizeAny(u8, in_content, "\n");
    while (line_iter.next()) |line| {
        const trimmed = std.mem.trim(u8, line, " \t\r\n");
        if (trimmed.len == 0 or trimmed[0] == '#') continue;

        var parts = std.mem.tokenizeScalar(u8, trimmed, ' ');
        const encoded_name = parts.next() orelse continue;
        const encoded_value = parts.next() orelse "";

        const name = base64Decode(allocator, encoded_name) catch {
            try stdout.print("Error decoding Base64 for name [{s}]\n", .{encoded_name});
            continue;
        };
        defer allocator.free(name);

        const value = if (encoded_value.len == 0)
            try allocator.alloc(u8, 0)
        else
            base64Decode(allocator, encoded_value) catch {
                try stdout.print("Error decoding Base64 for value [{s}]\n", .{encoded_value});
                continue;
            };
        defer allocator.free(value);

        try ipc_in_properties.put(try allocator.dupe(u8, name), try allocator.dupe(u8, value));
    }

    var it = ipc_in_properties.iterator();
    while (it.next()) |entry| {
        if (isValidUtf8(entry.key_ptr.*) and isValidUtf8(entry.value_ptr.*)) {
            try stdout.print("PIPELINER_IPC_IN variable [{s}] = [{s}]\n", .{
                entry.key_ptr.*, entry.value_ptr.*
            });
        } else {
            try stdout.print("PIPELINER_IPC_IN variable [{any}] = [{any}]\n", .{
                entry.key_ptr.*, entry.value_ptr.*
            });
        }
    }

    const ipc_out_properties = [_][2][]const u8{
        .{ "zig_extension_variable_1", "zig extension foo" },
        .{ "zig_extension_variable_2", "zig extension bar" },
    };

    var out_file = std.fs.cwd().createFile(ipc_out_file, .{}) catch {
        try stdout.print("Error: Unable to open PIPELINER_IPC_OUT file: {s}\n", .{ipc_out_file});
        std.process.exit(1);
    };
    defer out_file.close();

    var writer = out_file.writer();
    for (ipc_out_properties) |pair| {
        const key = pair[0];
        const value = pair[1];

        const encoded_key = try base64Encode(allocator, key);
        const encoded_value = if (value.len == 0) "" else try base64Encode(allocator, value);

        try stdout.print("PIPELINER_IPC_OUT variable [{s}] = [{s}]\n", .{ key, value });
        try writer.print("{s} {s}\n", .{ encoded_key, encoded_value });

        allocator.free(encoded_key);
        if (value.len != 0) allocator.free(encoded_value);
    }
}
