/*
 *
 *  * Copyright 2017-2023 Enedis
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.chutneytesting.environment.api.variable;

import com.chutneytesting.environment.api.variable.dto.EnvironmentVariableDto;
import com.chutneytesting.environment.domain.exception.VariableAlreadyExistingException;
import com.chutneytesting.environment.domain.exception.EnvVariableNotFoundException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class EnvironmentVariableController implements EnvironmentVariableApi {

    private final String TARGET_BASE_URI = "/api/v2/env-variables";
    private final EnvironmentVariableApi delegate;

    public EnvironmentVariableController(EnvironmentVariableApi delegate) {
        this.delegate = delegate;
    }


    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PostMapping(TARGET_BASE_URI)
    public void addVariable(@RequestBody List<EnvironmentVariableDto> values) throws EnvironmentNotFoundException, VariableAlreadyExistingException {
        delegate.addVariable(values);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PutMapping(TARGET_BASE_URI + "/{key}")
    public void updateVariable(@PathVariable("key") String key, @RequestBody List<EnvironmentVariableDto> values) throws EnvironmentNotFoundException, EnvVariableNotFoundException {
        delegate.updateVariable(key, values);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @DeleteMapping(TARGET_BASE_URI + "/{key}")
    public void deleteVariable(@PathVariable("key") String key) throws EnvVariableNotFoundException {
        delegate.deleteVariable(key);
    }
}
