/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.amqp.utils;

import static java.util.Spliterators.spliterator;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.StreamSupport;
import net.minidev.json.JSONArray;

public class JsonPathEvaluator {

    private static final Configuration CONFIG = Configuration.defaultConfiguration()
        .addOptions(Option.ALWAYS_RETURN_LIST)
        .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

    public static boolean evaluate(String jsonAsString, String jsonPath) {
        JSONArray ret = JsonPath.using(CONFIG).parse(jsonAsString).read(jsonPath);
        return StreamSupport.stream(spliterator(ret.iterator(), ret.size(), Spliterator.DISTINCT), false)
            .anyMatch(Objects::nonNull);
    }
}
