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

package com.chutneytesting.engine.infrastructure.delegation;

import static com.chutneytesting.engine.api.execution.HttpTestEngine.EXECUTION_URL;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.domain.delegation.CannotDelegateException;
import com.chutneytesting.engine.domain.delegation.ConnectionChecker;
import com.chutneytesting.engine.domain.delegation.DelegationClient;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.Dataset;
import com.chutneytesting.engine.domain.execution.engine.Environment;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/* TODO all -
    An agent receiving a scenario fragment with a finally action will execute that teardown as soon as it finnish.
    The complete scenario will not work due to this early unexpected teardown.
    Thus, Finally Actions should be driven by the main Agent executing the whole scenario.
*/
public class HttpClient implements DelegationClient {

    private final RestTemplate restTemplate;
    private final ConnectionChecker connectionChecker;

    public HttpClient() {
        this(null, null);
    }

    public HttpClient(String username, String password) {
        this.restTemplate = new RestTemplate();
        this.connectionChecker = new TcpConnectionChecker();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.findAndRegisterModules();

        restTemplate.setMessageConverters(Lists.newArrayList(new MappingJackson2HttpMessageConverter(objectMapper)));
        addBasicAuth(username, password);
    }

    @Override
    public StepExecutionReport handDown(Step step, NamedHostAndPort delegate) throws CannotDelegateException {
        if (connectionChecker.canConnectTo(delegate)) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Dataset dataset = new Dataset(emptyMap(), emptyList()); // TODO - check if it still works
            Environment environment =  new Environment((String) step.getScenarioContext().get("environment"));
            HttpEntity<ExecutionRequestDto> request = new HttpEntity<>(ExecutionRequestMapper.from(step.definition(), dataset, environment), headers);
            StepExecutionReportDto reportDto = restTemplate.postForObject("https://" + delegate.host() + ":" + delegate.port() + EXECUTION_URL, request, StepExecutionReportDto.class);
            return StepExecutionReportMapper.fromDto(reportDto);
        } else {
            throw new CannotDelegateException("Unable to connect to " + delegate.name() + " at " + delegate.host() + ":" + delegate.port());
        }
    }

    private void addBasicAuth(String user, String password) {
        if (ofNullable(user).isPresent()) {
            restTemplate.getInterceptors().add(
                new BasicAuthenticationInterceptor(user, password)
            );
        }
    }
}
