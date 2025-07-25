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

package org.verifyica.pipeliner.core.parser;

import java.util.Arrays;

/**
 * Auto-generated DFA using 2D array.
 */
public final class DFA {

    //
    // Supports the following words...
    //
    //        "
    //        #
    //        '
    //        */
    //        +:=
    //        /*
    //        //
    //        ::
    //        :=
    //        [
    //        ]
    //        capture
    //        env
    //        exec
    //        halt
    //        if
    //        print
    //        println
    //        shell
    //        sleep
    //        var
    //        working-directory
    //        {
    //        |
    //        }
    //

    private static final int[][] transitions = new int[75][128];
    private static final boolean[] terminal = new boolean[75];

    static {
        for (int[] transition : transitions) {
            Arrays.fill(transition, -1);
        }

        transitions[0]['"'] = 1;
        terminal[1] = true;
        transitions[0]['#'] = 2;
        terminal[2] = true;
        transitions[0]['\''] = 3;
        terminal[3] = true;
        transitions[0]['*'] = 4;
        transitions[4]['/'] = 5;
        terminal[5] = true;
        transitions[0]['+'] = 6;
        transitions[6][':'] = 7;
        transitions[7]['='] = 8;
        terminal[8] = true;
        transitions[0]['/'] = 9;
        transitions[9]['*'] = 10;
        terminal[10] = true;
        transitions[9]['/'] = 11;
        terminal[11] = true;
        transitions[0][':'] = 12;
        transitions[12][':'] = 13;
        terminal[13] = true;
        transitions[12]['='] = 14;
        terminal[14] = true;
        transitions[0]['['] = 15;
        terminal[15] = true;
        transitions[0][']'] = 16;
        terminal[16] = true;
        transitions[0]['c'] = 17;
        transitions[17]['a'] = 18;
        transitions[18]['p'] = 19;
        transitions[19]['t'] = 20;
        transitions[20]['u'] = 21;
        transitions[21]['r'] = 22;
        transitions[22]['e'] = 23;
        terminal[23] = true;
        transitions[0]['e'] = 24;
        transitions[24]['n'] = 25;
        transitions[25]['v'] = 26;
        terminal[26] = true;
        transitions[24]['x'] = 27;
        transitions[27]['e'] = 28;
        transitions[28]['c'] = 29;
        terminal[29] = true;
        transitions[0]['h'] = 30;
        transitions[30]['a'] = 31;
        transitions[31]['l'] = 32;
        transitions[32]['t'] = 33;
        terminal[33] = true;
        transitions[0]['i'] = 34;
        transitions[34]['f'] = 35;
        terminal[35] = true;
        transitions[0]['p'] = 36;
        transitions[36]['r'] = 37;
        transitions[37]['i'] = 38;
        transitions[38]['n'] = 39;
        transitions[39]['t'] = 40;
        terminal[40] = true;
        transitions[40]['l'] = 41;
        transitions[41]['n'] = 42;
        terminal[42] = true;
        transitions[0]['s'] = 43;
        transitions[43]['h'] = 44;
        transitions[44]['e'] = 45;
        transitions[45]['l'] = 46;
        transitions[46]['l'] = 47;
        terminal[47] = true;
        transitions[43]['l'] = 48;
        transitions[48]['e'] = 49;
        transitions[49]['e'] = 50;
        transitions[50]['p'] = 51;
        terminal[51] = true;
        transitions[0]['v'] = 52;
        transitions[52]['a'] = 53;
        transitions[53]['r'] = 54;
        terminal[54] = true;
        transitions[0]['w'] = 55;
        transitions[55]['o'] = 56;
        transitions[56]['r'] = 57;
        transitions[57]['k'] = 58;
        transitions[58]['i'] = 59;
        transitions[59]['n'] = 60;
        transitions[60]['g'] = 61;
        transitions[61]['-'] = 62;
        transitions[62]['d'] = 63;
        transitions[63]['i'] = 64;
        transitions[64]['r'] = 65;
        transitions[65]['e'] = 66;
        transitions[66]['c'] = 67;
        transitions[67]['t'] = 68;
        transitions[68]['o'] = 69;
        transitions[69]['r'] = 70;
        transitions[70]['y'] = 71;
        terminal[71] = true;
        transitions[0]['{'] = 72;
        terminal[72] = true;
        transitions[0]['|'] = 73;
        terminal[73] = true;
        transitions[0]['}'] = 74;
        terminal[74] = true;
    }

    /**
     * Constructor
     */
    private DFA() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Returns the length of the longest match in the given buffer from the specified start index to end index.
     *
     * @param buf the character buffer to search
     * @param start the starting index (inclusive)
     * @param end the ending index (exclusive)
     * @return the length of the longest match, or 0 if no match is found
     */
    public static int longestMatch(char[] buf, int start, int end) {
        int state = 0;
        int maxLength = 0;
        for (int i = start; i < end; i++) {
            char c = buf[i];
            if (c >= 128) break;
            int next = transitions[state][c];
            if (next == -1) break;
            state = next;
            if (terminal[state]) maxLength = i - start + 1;
        }
        return maxLength;
    }
}
