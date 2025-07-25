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

    private static final int[][] TRANSITIONS = new int[75][128];
    private static final boolean[] TERMINAL = new boolean[75];

    static {
        for (int[] transition : TRANSITIONS) {
            Arrays.fill(transition, -1);
        }

        TRANSITIONS[0]['"'] = 1;
        TERMINAL[1] = true;
        TRANSITIONS[0]['#'] = 2;
        TERMINAL[2] = true;
        TRANSITIONS[0]['\''] = 3;
        TERMINAL[3] = true;
        TRANSITIONS[0]['*'] = 4;
        TRANSITIONS[4]['/'] = 5;
        TERMINAL[5] = true;
        TRANSITIONS[0]['+'] = 6;
        TRANSITIONS[6][':'] = 7;
        TRANSITIONS[7]['='] = 8;
        TERMINAL[8] = true;
        TRANSITIONS[0]['/'] = 9;
        TRANSITIONS[9]['*'] = 10;
        TERMINAL[10] = true;
        TRANSITIONS[9]['/'] = 11;
        TERMINAL[11] = true;
        TRANSITIONS[0][':'] = 12;
        TRANSITIONS[12][':'] = 13;
        TERMINAL[13] = true;
        TRANSITIONS[12]['='] = 14;
        TERMINAL[14] = true;
        TRANSITIONS[0]['['] = 15;
        TERMINAL[15] = true;
        TRANSITIONS[0][']'] = 16;
        TERMINAL[16] = true;
        TRANSITIONS[0]['c'] = 17;
        TRANSITIONS[17]['a'] = 18;
        TRANSITIONS[18]['p'] = 19;
        TRANSITIONS[19]['t'] = 20;
        TRANSITIONS[20]['u'] = 21;
        TRANSITIONS[21]['r'] = 22;
        TRANSITIONS[22]['e'] = 23;
        TERMINAL[23] = true;
        TRANSITIONS[0]['e'] = 24;
        TRANSITIONS[24]['n'] = 25;
        TRANSITIONS[25]['v'] = 26;
        TERMINAL[26] = true;
        TRANSITIONS[24]['x'] = 27;
        TRANSITIONS[27]['e'] = 28;
        TRANSITIONS[28]['c'] = 29;
        TERMINAL[29] = true;
        TRANSITIONS[0]['h'] = 30;
        TRANSITIONS[30]['a'] = 31;
        TRANSITIONS[31]['l'] = 32;
        TRANSITIONS[32]['t'] = 33;
        TERMINAL[33] = true;
        TRANSITIONS[0]['i'] = 34;
        TRANSITIONS[34]['f'] = 35;
        TERMINAL[35] = true;
        TRANSITIONS[0]['p'] = 36;
        TRANSITIONS[36]['r'] = 37;
        TRANSITIONS[37]['i'] = 38;
        TRANSITIONS[38]['n'] = 39;
        TRANSITIONS[39]['t'] = 40;
        TERMINAL[40] = true;
        TRANSITIONS[40]['l'] = 41;
        TRANSITIONS[41]['n'] = 42;
        TERMINAL[42] = true;
        TRANSITIONS[0]['s'] = 43;
        TRANSITIONS[43]['h'] = 44;
        TRANSITIONS[44]['e'] = 45;
        TRANSITIONS[45]['l'] = 46;
        TRANSITIONS[46]['l'] = 47;
        TERMINAL[47] = true;
        TRANSITIONS[43]['l'] = 48;
        TRANSITIONS[48]['e'] = 49;
        TRANSITIONS[49]['e'] = 50;
        TRANSITIONS[50]['p'] = 51;
        TERMINAL[51] = true;
        TRANSITIONS[0]['v'] = 52;
        TRANSITIONS[52]['a'] = 53;
        TRANSITIONS[53]['r'] = 54;
        TERMINAL[54] = true;
        TRANSITIONS[0]['w'] = 55;
        TRANSITIONS[55]['o'] = 56;
        TRANSITIONS[56]['r'] = 57;
        TRANSITIONS[57]['k'] = 58;
        TRANSITIONS[58]['i'] = 59;
        TRANSITIONS[59]['n'] = 60;
        TRANSITIONS[60]['g'] = 61;
        TRANSITIONS[61]['-'] = 62;
        TRANSITIONS[62]['d'] = 63;
        TRANSITIONS[63]['i'] = 64;
        TRANSITIONS[64]['r'] = 65;
        TRANSITIONS[65]['e'] = 66;
        TRANSITIONS[66]['c'] = 67;
        TRANSITIONS[67]['t'] = 68;
        TRANSITIONS[68]['o'] = 69;
        TRANSITIONS[69]['r'] = 70;
        TRANSITIONS[70]['y'] = 71;
        TERMINAL[71] = true;
        TRANSITIONS[0]['{'] = 72;
        TERMINAL[72] = true;
        TRANSITIONS[0]['|'] = 73;
        TERMINAL[73] = true;
        TRANSITIONS[0]['}'] = 74;
        TERMINAL[74] = true;
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
     * @param characters the character buffer to search
     * @param start the starting index (inclusive)
     * @param end the ending index (exclusive)
     * @return the length of the longest match, or 0 if no match is found
     */
    public static int longestMatch(char[] characters, int start, int end) {
        int state = 0;
        int maximumLength = 0;
        for (int i = start; i < end; i++) {
            char c = characters[i];
            if (c >= 128) break;
            int next = TRANSITIONS[state][c];
            if (next == -1) break;
            state = next;
            if (TERMINAL[state]) maximumLength = i - start + 1;
        }
        return maximumLength;
    }
}
