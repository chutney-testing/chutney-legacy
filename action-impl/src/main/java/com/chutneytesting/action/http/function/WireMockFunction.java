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
