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

package com.chutneytesting.environment.api.target;

import com.chutneytesting.environment.api.target.dto.TargetDto;
import com.chutneytesting.environment.domain.TargetFilter;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
public class TargetController implements TargetApi {

    private final String ENVIRONMENT_BASE_URI = "/api/v2/environments";
    private final String TARGET_BASE_URI = "/api/v2/targets";
    private final TargetApi delegate;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    TargetController(TargetApi delegate) {
        this.delegate = delegate;
    }

    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PostMapping(value = ENVIRONMENT_BASE_URI + "/{environmentName}/targets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TargetDto importTarget(@PathVariable("environmentName") String environmentName, @RequestParam("file") MultipartFile file) {
        try {
            return importTarget(
                environmentName,
                objectMapper.readValue(file.getBytes(), TargetDto.class)
            );
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot deserialize file: " + file.getName(), e);
        }
    }

    @Override
    public TargetDto importTarget(String environmentName, TargetDto targetDto) {
        return delegate.importTarget(environmentName, targetDto);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping(path = TARGET_BASE_URI + "/names", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> listTargetsNames() throws EnvironmentNotFoundException {
        return delegate.listTargetsNames();
    }

    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping(path = TARGET_BASE_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TargetDto> listTargets(TargetFilter filter) throws EnvironmentNotFoundException {
        return delegate.listTargets(filter);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping(ENVIRONMENT_BASE_URI + "/{environmentName}/targets/{targetName}")
    public TargetDto getTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        return delegate.getTarget(environmentName, targetName);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @DeleteMapping(ENVIRONMENT_BASE_URI+ "/{environmentName}/targets/{targetName}")
    public void deleteTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        delegate.deleteTarget(environmentName, targetName);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @DeleteMapping(TARGET_BASE_URI+ "/{targetName}")
    public void deleteTarget(@PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        delegate.deleteTarget(targetName);
    }


    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PostMapping(TARGET_BASE_URI)
    public void addTarget(@RequestBody TargetDto targetDto) throws EnvironmentNotFoundException, AlreadyExistingTargetException {
        delegate.addTarget(targetDto);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PutMapping(TARGET_BASE_URI + "/{targetName}")
    public void updateTarget(@PathVariable("targetName") String targetName, @RequestBody TargetDto targetDto) throws EnvironmentNotFoundException, TargetNotFoundException {
        delegate.updateTarget(targetName, targetDto);
    }
}
