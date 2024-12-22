/**
 * Copyright 2012 Kamran Zafar 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */

/**
 * Copyright 2024 xiaoxindada
 *
 * https://github.com/xiaoxindada/jtar
 */

package c4451848.org.kamranzafar.jtar;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Kamran Zafar, John Wu
 *
 */
@SuppressWarnings("PMD.UselessParentheses")
public class Octal {

    // 8 ^ 11 - 1
    private static final long OCTAL_MAX = 8589934591L;
    private static final byte LARGE_NUM_MASK = (byte) 0x80;

    /**
     * Parse an octal string from a header buffer.
     *
     * @param header
     *            The header buffer from which to parse.
     * @param offset
     *            The offset into the buffer from which to parse.
     * @param length
     *            The number of header bytes to parse.
     * 
     * @return The long value of the octal string.
     */
    public static long parseOctal(byte[] header, int offset, int length) {
        long result = 0;
        boolean stillPadding = true;

        int end = offset + length;
        for (int i = offset; i < end; ++i) {
            byte b = header[i];

            if ((b & LARGE_NUM_MASK) != 0 && length == 12) {
                // Read the lower 8 bytes as big-endian long value
                return ByteBuffer.wrap(header, offset + 4, 8).order(ByteOrder.BIG_ENDIAN).getLong();
            }

            if (b == 0)
                break;

            if (b == (byte) ' ' || b == '0') {
                if (stillPadding)
                    continue;

                if (b == (byte) ' ')
                    break;
            }

            stillPadding = false;

            result = ( result << 3 ) + ( b - '0' );
        }

        return result;
    }

    /**
     * Write an octal integer to a header buffer.
     * 
     * @param value
     *            The value to write.
     * @param buf
     *            The header buffer from which to parse.
     * @param offset
     *            The offset into the buffer from which to parse.
     * @param length
     *            The number of header bytes to parse.
     * 
     * @return The new offset.
     */
    public static int getOctalBytes(long value, byte[] buf, int offset, int length) {
        if (value > OCTAL_MAX && length == 12) {
            buf[offset] = LARGE_NUM_MASK;
            buf[offset + 1] = 0;
            buf[offset + 2] = 0;
            buf[offset + 3] = 0;
            ByteBuffer.wrap(buf, offset + 4, 8).order(ByteOrder.BIG_ENDIAN).putLong(value);
            return offset + length;
        }

        int idx = length - 1;

        buf[offset + idx] = 0;
        --idx;

        for (long val = value; idx >= 0; --idx) {
            buf[offset + idx] = (byte) ((byte) '0' + (byte) (val & 7));
            val = val >> 3;
        }

        return offset + length;
    }

    /**
     * Write the checksum octal integer to a header buffer.
     *
     * @param value
     *            The value to write.
     * @param buf
     *            The header buffer from which to parse.
     * @param offset
     *            The offset into the buffer from which to parse.
     * @param length
     *            The number of header bytes to parse.
     * @return The new offset.
     */
    public static int getCheckSumOctalBytes(long value, byte[] buf, int offset, int length) {
        getOctalBytes(value, buf, offset, length - 1);
        buf[offset + length - 1] = (byte) ' ';
        return offset + length;
    }

}
