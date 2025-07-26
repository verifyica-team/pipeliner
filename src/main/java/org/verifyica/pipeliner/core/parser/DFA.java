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
    //        #{
    //        $
    //        ${
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
    //        \#{
    //        \$
    //        \${
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

    private static final int[][] TRANSITIONS = new int[105][128];
    private static final boolean[] TERMINAL = new boolean[105];

    static {
        for (int[] transition : TRANSITIONS) {
            Arrays.fill(transition, -1);
        }

        TRANSITIONS[0]['"'] = 1;
        TERMINAL[1] = true;
        TRANSITIONS[0]['#'] = 2;
        TERMINAL[2] = true;
        TRANSITIONS[2]['{'] = 3;
        TERMINAL[3] = true;
        TRANSITIONS[0]['$'] = 4;
        TERMINAL[4] = true;
        TRANSITIONS[4]['{'] = 5;
        TERMINAL[5] = true;
        TRANSITIONS[0]['\''] = 6;
        TERMINAL[6] = true;
        TRANSITIONS[0]['('] = 7;
        TERMINAL[7] = true;
        TRANSITIONS[0][')'] = 8;
        TERMINAL[8] = true;
        TRANSITIONS[0]['*'] = 9;
        TRANSITIONS[9]['/'] = 10;
        TERMINAL[10] = true;
        TRANSITIONS[0]['+'] = 11;
        TRANSITIONS[11][':'] = 12;
        TRANSITIONS[12]['='] = 13;
        TERMINAL[13] = true;
        TRANSITIONS[0]['/'] = 14;
        TRANSITIONS[14]['*'] = 15;
        TERMINAL[15] = true;
        TRANSITIONS[14]['/'] = 16;
        TERMINAL[16] = true;
        TRANSITIONS[0][':'] = 17;
        TRANSITIONS[17][':'] = 18;
        TERMINAL[18] = true;
        TRANSITIONS[17]['='] = 19;
        TERMINAL[19] = true;
        TRANSITIONS[0]['['] = 20;
        TERMINAL[20] = true;
        TRANSITIONS[0]['\\'] = 21;
        TRANSITIONS[21]['#'] = 22;
        TRANSITIONS[22]['{'] = 23;
        TERMINAL[23] = true;
        TRANSITIONS[21]['$'] = 24;
        TERMINAL[24] = true;
        TRANSITIONS[24]['{'] = 25;
        TERMINAL[25] = true;
        TRANSITIONS[0][']'] = 26;
        TERMINAL[26] = true;
        TRANSITIONS[0]['e'] = 27;
        TRANSITIONS[27]['n'] = 28;
        TRANSITIONS[28]['v'] = 29;
        TERMINAL[29] = true;
        TRANSITIONS[29]['i'] = 30;
        TRANSITIONS[30]['r'] = 31;
        TRANSITIONS[31]['o'] = 32;
        TRANSITIONS[32]['n'] = 33;
        TRANSITIONS[33]['m'] = 34;
        TRANSITIONS[34]['e'] = 35;
        TRANSITIONS[35]['n'] = 36;
        TRANSITIONS[36]['t'] = 37;
        TRANSITIONS[37]['-'] = 38;
        TRANSITIONS[38]['v'] = 39;
        TRANSITIONS[39]['a'] = 40;
        TRANSITIONS[40]['r'] = 41;
        TRANSITIONS[41]['i'] = 42;
        TRANSITIONS[42]['a'] = 43;
        TRANSITIONS[43]['b'] = 44;
        TRANSITIONS[44]['l'] = 45;
        TRANSITIONS[45]['e'] = 46;
        TERMINAL[46] = true;
        TRANSITIONS[27]['x'] = 47;
        TRANSITIONS[47]['e'] = 48;
        TRANSITIONS[48]['c'] = 49;
        TERMINAL[49] = true;
        TRANSITIONS[49]['u'] = 50;
        TRANSITIONS[50]['t'] = 51;
        TRANSITIONS[51]['e'] = 52;
        TERMINAL[52] = true;
        TRANSITIONS[0]['h'] = 53;
        TRANSITIONS[53]['a'] = 54;
        TRANSITIONS[54]['l'] = 55;
        TRANSITIONS[55]['t'] = 56;
        TERMINAL[56] = true;
        TRANSITIONS[0]['i'] = 57;
        TRANSITIONS[57]['f'] = 58;
        TERMINAL[58] = true;
        TRANSITIONS[0]['p'] = 59;
        TRANSITIONS[59]['r'] = 60;
        TRANSITIONS[60]['i'] = 61;
        TRANSITIONS[61]['n'] = 62;
        TRANSITIONS[62]['t'] = 63;
        TERMINAL[63] = true;
        TRANSITIONS[63]['l'] = 64;
        TRANSITIONS[64]['n'] = 65;
        TERMINAL[65] = true;
        TRANSITIONS[0]['s'] = 66;
        TRANSITIONS[66]['h'] = 67;
        TRANSITIONS[67]['e'] = 68;
        TRANSITIONS[68]['l'] = 69;
        TRANSITIONS[69]['l'] = 70;
        TERMINAL[70] = true;
        TRANSITIONS[66]['l'] = 71;
        TRANSITIONS[71]['e'] = 72;
        TRANSITIONS[72]['e'] = 73;
        TRANSITIONS[73]['p'] = 74;
        TERMINAL[74] = true;
        TRANSITIONS[66]['t'] = 75;
        TRANSITIONS[75]['r'] = 76;
        TERMINAL[76] = true;
        TRANSITIONS[0]['v'] = 77;
        TRANSITIONS[77]['a'] = 78;
        TRANSITIONS[78]['r'] = 79;
        TERMINAL[79] = true;
        TRANSITIONS[79]['i'] = 80;
        TRANSITIONS[80]['a'] = 81;
        TRANSITIONS[81]['b'] = 82;
        TRANSITIONS[82]['l'] = 83;
        TRANSITIONS[83]['e'] = 84;
        TERMINAL[84] = true;
        TRANSITIONS[0]['w'] = 85;
        TRANSITIONS[85]['o'] = 86;
        TRANSITIONS[86]['r'] = 87;
        TRANSITIONS[87]['k'] = 88;
        TRANSITIONS[88]['i'] = 89;
        TRANSITIONS[89]['n'] = 90;
        TRANSITIONS[90]['g'] = 91;
        TRANSITIONS[91]['-'] = 92;
        TRANSITIONS[92]['d'] = 93;
        TRANSITIONS[93]['i'] = 94;
        TRANSITIONS[94]['r'] = 95;
        TRANSITIONS[95]['e'] = 96;
        TRANSITIONS[96]['c'] = 97;
        TRANSITIONS[97]['t'] = 98;
        TRANSITIONS[98]['o'] = 99;
        TRANSITIONS[99]['r'] = 100;
        TRANSITIONS[100]['y'] = 101;
        TERMINAL[101] = true;
        TRANSITIONS[0]['{'] = 102;
        TERMINAL[102] = true;
        TRANSITIONS[0]['|'] = 103;
        TERMINAL[103] = true;
        TRANSITIONS[0]['}'] = 104;
        TERMINAL[104] = true;
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
