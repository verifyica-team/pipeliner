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

start
    : (line NEWLINE)* EOF
    ;

line
    : (PROPERTY | ENVIRONMENT_VARIABLE | ENVIRONMENT_VARIABLE_WITH_BRACES | ESCAPED_DOLLAR | BACKSLASH | DOLLAR | TEXT)*  // A line is a sequence of tokens
    ;

PROPERTY
    : '${{' ~('\\' | '}')* [ \t]* [a-zA-Z_][a-zA-Z0-9._-]* [ \t]* '}}'  // Match ${{ foo }} but not \${{ foo }}
    ;

ENVIRONMENT_VARIABLE
    : '$' [a-zA-Z_][a-zA-Z0-9_]*  // Match $FOO format
    ;

ENVIRONMENT_VARIABLE_WITH_BRACES
    : '${' [a-zA-Z_][a-zA-Z0-9_-]* '}'  // Match ${bar} format
    ;

ESCAPED_DOLLAR
    : '\\${{'  // Match the exact sequence '\${{'
    | '\\${'  // Match the exact sequence '\${'
    | '\\$'  // Match the exact sequence '\$'
    ;

BACKSLASH
    : '\\'  // Match the exact character '\'
    ;

DOLLAR
    : '$'  // Match the exact character '$'
    ;

TEXT
    : ~[\n$\\{]+  // Match any text, treating \ in front of $ as part of TEXT
    ;

NEWLINE
    : [\r\n]+  // Match new lines
    ;

WS
    : [ \t]+ -> skip  // Whitespace is skipped
    ;
