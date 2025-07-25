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
    //        env
    //        environment-variable
    //        exec
    //        execute
    //        halt
    //        if
    //        print
    //        println
    //        shell
    //        sleep
    //        var
    //        variable
    //        working-directory
    //        {
    //        |
    //        }
    //

    private static final int[][] TRANSITIONS = new int[93][128];
    private static final boolean[] TERMINAL = new boolean[93];

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
        TRANSITIONS[0]['e'] = 17;
        TRANSITIONS[17]['n'] = 18;
        TRANSITIONS[18]['v'] = 19;
        TERMINAL[19] = true;
        TRANSITIONS[19]['i'] = 20;
        TRANSITIONS[20]['r'] = 21;
        TRANSITIONS[21]['o'] = 22;
        TRANSITIONS[22]['n'] = 23;
        TRANSITIONS[23]['m'] = 24;
        TRANSITIONS[24]['e'] = 25;
        TRANSITIONS[25]['n'] = 26;
        TRANSITIONS[26]['t'] = 27;
        TRANSITIONS[27]['-'] = 28;
        TRANSITIONS[28]['v'] = 29;
        TRANSITIONS[29]['a'] = 30;
        TRANSITIONS[30]['r'] = 31;
        TRANSITIONS[31]['i'] = 32;
        TRANSITIONS[32]['a'] = 33;
        TRANSITIONS[33]['b'] = 34;
        TRANSITIONS[34]['l'] = 35;
        TRANSITIONS[35]['e'] = 36;
        TERMINAL[36] = true;
        TRANSITIONS[17]['x'] = 37;
        TRANSITIONS[37]['e'] = 38;
        TRANSITIONS[38]['c'] = 39;
        TERMINAL[39] = true;
        TRANSITIONS[39]['u'] = 40;
        TRANSITIONS[40]['t'] = 41;
        TRANSITIONS[41]['e'] = 42;
        TERMINAL[42] = true;
        TRANSITIONS[0]['h'] = 43;
        TRANSITIONS[43]['a'] = 44;
        TRANSITIONS[44]['l'] = 45;
        TRANSITIONS[45]['t'] = 46;
        TERMINAL[46] = true;
        TRANSITIONS[0]['i'] = 47;
        TRANSITIONS[47]['f'] = 48;
        TERMINAL[48] = true;
        TRANSITIONS[0]['p'] = 49;
        TRANSITIONS[49]['r'] = 50;
        TRANSITIONS[50]['i'] = 51;
        TRANSITIONS[51]['n'] = 52;
        TRANSITIONS[52]['t'] = 53;
        TERMINAL[53] = true;
        TRANSITIONS[53]['l'] = 54;
        TRANSITIONS[54]['n'] = 55;
        TERMINAL[55] = true;
        TRANSITIONS[0]['s'] = 56;
        TRANSITIONS[56]['h'] = 57;
        TRANSITIONS[57]['e'] = 58;
        TRANSITIONS[58]['l'] = 59;
        TRANSITIONS[59]['l'] = 60;
        TERMINAL[60] = true;
        TRANSITIONS[56]['l'] = 61;
        TRANSITIONS[61]['e'] = 62;
        TRANSITIONS[62]['e'] = 63;
        TRANSITIONS[63]['p'] = 64;
        TERMINAL[64] = true;
        TRANSITIONS[0]['v'] = 65;
        TRANSITIONS[65]['a'] = 66;
        TRANSITIONS[66]['r'] = 67;
        TERMINAL[67] = true;
        TRANSITIONS[67]['i'] = 68;
        TRANSITIONS[68]['a'] = 69;
        TRANSITIONS[69]['b'] = 70;
        TRANSITIONS[70]['l'] = 71;
        TRANSITIONS[71]['e'] = 72;
        TERMINAL[72] = true;
        TRANSITIONS[0]['w'] = 73;
        TRANSITIONS[73]['o'] = 74;
        TRANSITIONS[74]['r'] = 75;
        TRANSITIONS[75]['k'] = 76;
        TRANSITIONS[76]['i'] = 77;
        TRANSITIONS[77]['n'] = 78;
        TRANSITIONS[78]['g'] = 79;
        TRANSITIONS[79]['-'] = 80;
        TRANSITIONS[80]['d'] = 81;
        TRANSITIONS[81]['i'] = 82;
        TRANSITIONS[82]['r'] = 83;
        TRANSITIONS[83]['e'] = 84;
        TRANSITIONS[84]['c'] = 85;
        TRANSITIONS[85]['t'] = 86;
        TRANSITIONS[86]['o'] = 87;
        TRANSITIONS[87]['r'] = 88;
        TRANSITIONS[88]['y'] = 89;
        TERMINAL[89] = true;
        TRANSITIONS[0]['{'] = 90;
        TERMINAL[90] = true;
        TRANSITIONS[0]['|'] = 91;
        TERMINAL[91] = true;
        TRANSITIONS[0]['}'] = 92;
        TERMINAL[92] = true;
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
