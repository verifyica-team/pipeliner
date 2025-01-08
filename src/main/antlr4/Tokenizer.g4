/*
 * Copyright (C) 2025-present Pipeliner project authors and contributors
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

grammar Tokenizer;

LEFT_BRACE
    : '{'
    ;

RIGHT_BRACE
    : '}'
    ;

DOLLAR
    : '$'
    ;

BACKSLASH
    : '\\'
    ;

QUOTE
    : '\''
    ;

DOUBLE_QUOTE
    : '"'
    ;

TEXT
    : ~[\n\r$\\{}']+
    ;

NEWLINE
    : [\r\n]+
    ;

SPACES
    : [ \t]+
    ;

WS
    : [ \t]+ -> skip
    ;

start
    : (line NEWLINE?)* EOF
    ;

variable
    : DOLLAR (LEFT_BRACE LEFT_BRACE SPACES? TEXT SPACES? RIGHT_BRACE RIGHT_BRACE)
    | DOLLAR (LEFT_BRACE TEXT RIGHT_BRACE)
    | DOLLAR (TEXT)
    | BACKSLASH (DOLLAR LEFT_BRACE LEFT_BRACE SPACES? TEXT SPACES? RIGHT_BRACE RIGHT_BRACE)
    | BACKSLASH (DOLLAR LEFT_BRACE TEXT RIGHT_BRACE)
    | BACKSLASH (DOLLAR TEXT)
    ;

backslash
    : BACKSLASH
    ;

backslashDoubleQuote
    : BACKSLASH (DOUBLE_QUOTE)
    ;

dollar
    : DOLLAR
    ;

leftParenthesis
    : LEFT_BRACE
    ;

rightParenthesis
    : RIGHT_BRACE
    ;

quote
    : QUOTE
    ;

doubleQuote
    : DOUBLE_QUOTE
    ;

text
    : TEXT
    ;

line
    : (variable | leftParenthesis | rightParenthesis | quote | backslashDoubleQuote | doubleQuote | dollar | backslash | text)
    ;
