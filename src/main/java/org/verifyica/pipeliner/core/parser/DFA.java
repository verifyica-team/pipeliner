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
    //        global
    //        halt
    //        if
    //        print
    //        println
    //        shell
    //        sleep
    //        str
    //        var
    //        variable
    //        work-dir
    //        working-directory
    //        {
    //        |
    //        }
    //

    private static final int[][] TRANSITIONS = new int[115][128];
    private static final boolean[] TERMINAL = new boolean[115];

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
        TRANSITIONS[0]['g'] = 53;
        TRANSITIONS[53]['l'] = 54;
        TRANSITIONS[54]['o'] = 55;
        TRANSITIONS[55]['b'] = 56;
        TRANSITIONS[56]['a'] = 57;
        TRANSITIONS[57]['l'] = 58;
        TERMINAL[58] = true;
        TRANSITIONS[0]['h'] = 59;
        TRANSITIONS[59]['a'] = 60;
        TRANSITIONS[60]['l'] = 61;
        TRANSITIONS[61]['t'] = 62;
        TERMINAL[62] = true;
        TRANSITIONS[0]['i'] = 63;
        TRANSITIONS[63]['f'] = 64;
        TERMINAL[64] = true;
        TRANSITIONS[0]['p'] = 65;
        TRANSITIONS[65]['r'] = 66;
        TRANSITIONS[66]['i'] = 67;
        TRANSITIONS[67]['n'] = 68;
        TRANSITIONS[68]['t'] = 69;
        TERMINAL[69] = true;
        TRANSITIONS[69]['l'] = 70;
        TRANSITIONS[70]['n'] = 71;
        TERMINAL[71] = true;
        TRANSITIONS[0]['s'] = 72;
        TRANSITIONS[72]['h'] = 73;
        TRANSITIONS[73]['e'] = 74;
        TRANSITIONS[74]['l'] = 75;
        TRANSITIONS[75]['l'] = 76;
        TERMINAL[76] = true;
        TRANSITIONS[72]['l'] = 77;
        TRANSITIONS[77]['e'] = 78;
        TRANSITIONS[78]['e'] = 79;
        TRANSITIONS[79]['p'] = 80;
        TERMINAL[80] = true;
        TRANSITIONS[72]['t'] = 81;
        TRANSITIONS[81]['r'] = 82;
        TERMINAL[82] = true;
        TRANSITIONS[0]['v'] = 83;
        TRANSITIONS[83]['a'] = 84;
        TRANSITIONS[84]['r'] = 85;
        TERMINAL[85] = true;
        TRANSITIONS[85]['i'] = 86;
        TRANSITIONS[86]['a'] = 87;
        TRANSITIONS[87]['b'] = 88;
        TRANSITIONS[88]['l'] = 89;
        TRANSITIONS[89]['e'] = 90;
        TERMINAL[90] = true;
        TRANSITIONS[0]['w'] = 91;
        TRANSITIONS[91]['o'] = 92;
        TRANSITIONS[92]['r'] = 93;
        TRANSITIONS[93]['k'] = 94;
        TRANSITIONS[94]['-'] = 95;
        TRANSITIONS[95]['d'] = 96;
        TRANSITIONS[96]['i'] = 97;
        TRANSITIONS[97]['r'] = 98;
        TERMINAL[98] = true;
        TRANSITIONS[94]['i'] = 99;
        TRANSITIONS[99]['n'] = 100;
        TRANSITIONS[100]['g'] = 101;
        TRANSITIONS[101]['-'] = 102;
        TRANSITIONS[102]['d'] = 103;
        TRANSITIONS[103]['i'] = 104;
        TRANSITIONS[104]['r'] = 105;
        TRANSITIONS[105]['e'] = 106;
        TRANSITIONS[106]['c'] = 107;
        TRANSITIONS[107]['t'] = 108;
        TRANSITIONS[108]['o'] = 109;
        TRANSITIONS[109]['r'] = 110;
        TRANSITIONS[110]['y'] = 111;
        TERMINAL[111] = true;
        TRANSITIONS[0]['{'] = 112;
        TERMINAL[112] = true;
        TRANSITIONS[0]['|'] = 113;
        TERMINAL[113] = true;
        TRANSITIONS[0]['}'] = 114;
        TERMINAL[114] = true;
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
