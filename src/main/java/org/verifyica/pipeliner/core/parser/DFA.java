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
    //        (
    //        )
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
    //        str
    //        var
    //        variable
    //        working-directory
    //        {
    //        |
    //        }
    //

    private static final int[][] TRANSITIONS = new int[97][128];
    private static final boolean[] TERMINAL = new boolean[97];

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
        TRANSITIONS[0]['('] = 4;
        TERMINAL[4] = true;
        TRANSITIONS[0][')'] = 5;
        TERMINAL[5] = true;
        TRANSITIONS[0]['*'] = 6;
        TRANSITIONS[6]['/'] = 7;
        TERMINAL[7] = true;
        TRANSITIONS[0]['+'] = 8;
        TRANSITIONS[8][':'] = 9;
        TRANSITIONS[9]['='] = 10;
        TERMINAL[10] = true;
        TRANSITIONS[0]['/'] = 11;
        TRANSITIONS[11]['*'] = 12;
        TERMINAL[12] = true;
        TRANSITIONS[11]['/'] = 13;
        TERMINAL[13] = true;
        TRANSITIONS[0][':'] = 14;
        TRANSITIONS[14][':'] = 15;
        TERMINAL[15] = true;
        TRANSITIONS[14]['='] = 16;
        TERMINAL[16] = true;
        TRANSITIONS[0]['['] = 17;
        TERMINAL[17] = true;
        TRANSITIONS[0][']'] = 18;
        TERMINAL[18] = true;
        TRANSITIONS[0]['e'] = 19;
        TRANSITIONS[19]['n'] = 20;
        TRANSITIONS[20]['v'] = 21;
        TERMINAL[21] = true;
        TRANSITIONS[21]['i'] = 22;
        TRANSITIONS[22]['r'] = 23;
        TRANSITIONS[23]['o'] = 24;
        TRANSITIONS[24]['n'] = 25;
        TRANSITIONS[25]['m'] = 26;
        TRANSITIONS[26]['e'] = 27;
        TRANSITIONS[27]['n'] = 28;
        TRANSITIONS[28]['t'] = 29;
        TRANSITIONS[29]['-'] = 30;
        TRANSITIONS[30]['v'] = 31;
        TRANSITIONS[31]['a'] = 32;
        TRANSITIONS[32]['r'] = 33;
        TRANSITIONS[33]['i'] = 34;
        TRANSITIONS[34]['a'] = 35;
        TRANSITIONS[35]['b'] = 36;
        TRANSITIONS[36]['l'] = 37;
        TRANSITIONS[37]['e'] = 38;
        TERMINAL[38] = true;
        TRANSITIONS[19]['x'] = 39;
        TRANSITIONS[39]['e'] = 40;
        TRANSITIONS[40]['c'] = 41;
        TERMINAL[41] = true;
        TRANSITIONS[41]['u'] = 42;
        TRANSITIONS[42]['t'] = 43;
        TRANSITIONS[43]['e'] = 44;
        TERMINAL[44] = true;
        TRANSITIONS[0]['h'] = 45;
        TRANSITIONS[45]['a'] = 46;
        TRANSITIONS[46]['l'] = 47;
        TRANSITIONS[47]['t'] = 48;
        TERMINAL[48] = true;
        TRANSITIONS[0]['i'] = 49;
        TRANSITIONS[49]['f'] = 50;
        TERMINAL[50] = true;
        TRANSITIONS[0]['p'] = 51;
        TRANSITIONS[51]['r'] = 52;
        TRANSITIONS[52]['i'] = 53;
        TRANSITIONS[53]['n'] = 54;
        TRANSITIONS[54]['t'] = 55;
        TERMINAL[55] = true;
        TRANSITIONS[55]['l'] = 56;
        TRANSITIONS[56]['n'] = 57;
        TERMINAL[57] = true;
        TRANSITIONS[0]['s'] = 58;
        TRANSITIONS[58]['h'] = 59;
        TRANSITIONS[59]['e'] = 60;
        TRANSITIONS[60]['l'] = 61;
        TRANSITIONS[61]['l'] = 62;
        TERMINAL[62] = true;
        TRANSITIONS[58]['l'] = 63;
        TRANSITIONS[63]['e'] = 64;
        TRANSITIONS[64]['e'] = 65;
        TRANSITIONS[65]['p'] = 66;
        TERMINAL[66] = true;
        TRANSITIONS[58]['t'] = 67;
        TRANSITIONS[67]['r'] = 68;
        TERMINAL[68] = true;
        TRANSITIONS[0]['v'] = 69;
        TRANSITIONS[69]['a'] = 70;
        TRANSITIONS[70]['r'] = 71;
        TERMINAL[71] = true;
        TRANSITIONS[71]['i'] = 72;
        TRANSITIONS[72]['a'] = 73;
        TRANSITIONS[73]['b'] = 74;
        TRANSITIONS[74]['l'] = 75;
        TRANSITIONS[75]['e'] = 76;
        TERMINAL[76] = true;
        TRANSITIONS[0]['w'] = 77;
        TRANSITIONS[77]['o'] = 78;
        TRANSITIONS[78]['r'] = 79;
        TRANSITIONS[79]['k'] = 80;
        TRANSITIONS[80]['i'] = 81;
        TRANSITIONS[81]['n'] = 82;
        TRANSITIONS[82]['g'] = 83;
        TRANSITIONS[83]['-'] = 84;
        TRANSITIONS[84]['d'] = 85;
        TRANSITIONS[85]['i'] = 86;
        TRANSITIONS[86]['r'] = 87;
        TRANSITIONS[87]['e'] = 88;
        TRANSITIONS[88]['c'] = 89;
        TRANSITIONS[89]['t'] = 90;
        TRANSITIONS[90]['o'] = 91;
        TRANSITIONS[91]['r'] = 92;
        TRANSITIONS[92]['y'] = 93;
        TERMINAL[93] = true;
        TRANSITIONS[0]['{'] = 94;
        TERMINAL[94] = true;
        TRANSITIONS[0]['|'] = 95;
        TERMINAL[95] = true;
        TRANSITIONS[0]['}'] = 96;
        TERMINAL[96] = true;
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
