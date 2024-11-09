/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.pipeline.common;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class YamlHelper {

    private YamlHelper() {
        // INTENTIONALLY BLANK
    }

    public static boolean asBoolean(Object object, boolean defaultValue) {
        if (object == null) {
            return defaultValue;
        }

        if (!"true".equals(object.toString())) {
            return false;
        }

        return true;
    }

    public static String asString(Object object) {
        return (String) object;
    }

    public static Map<Object, Object> asMap(Object object) {
        return (Map<Object, Object>) object;
    }

    public static List<Object> asList(Object object) {
        return (List<Object>) object;
    }
}
