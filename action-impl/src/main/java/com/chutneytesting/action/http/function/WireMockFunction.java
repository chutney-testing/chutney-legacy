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

package com.chutneytesting.action.http.function;

import static java.util.stream.Collectors.toMap;

import com.chutneytesting.action.spi.SpelFunction;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class WireMockFunction {

    @Deprecated
    @SpelFunction
    public static Map<String, String> extractHeadersAsMap(LoggedRequest request) {
        return wiremockHeaders(request);
    }

    @SpelFunction
    public static Map<String, String> wiremockHeaders(LoggedRequest request) {
        return request.getHeaders().all().stream()
            .collect(toMap(MultiValue::key, WireMockFunction::wiremockMultiValueJoin));
    }

    @Deprecated
    @SpelFunction
    public static Map<String, String> extractParameters(LoggedRequest request) {
        return wiremockQueryParams(request);
    }

    @SpelFunction
    public static Map<String, String> wiremockQueryParams(LoggedRequest request) {
        return request.getQueryParams().values().stream()
            .collect(toMap(MultiValue::key, WireMockFunction::wiremockMultiValueJoin));
    }

    private static String wiremockMultiValueJoin(MultiValue mv) {
        return StringUtils.join(mv.values(), ", ");
    }
}
