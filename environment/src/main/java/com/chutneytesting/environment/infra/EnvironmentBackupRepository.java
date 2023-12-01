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

package com.chutneytesting.environment.infra;

import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.EnvironmentApi;
import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.server.core.domain.admin.Backupable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentBackupRepository implements Backupable {

    private final EnvironmentApi embeddedEnvironmentApi;

    private final ObjectMapper om = new ObjectMapper()
        .findAndRegisterModules()
        .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    public EnvironmentBackupRepository(EmbeddedEnvironmentApi embeddedEnvironmentApi) {
        this.embeddedEnvironmentApi = embeddedEnvironmentApi;
    }

    @Override
    public void backup(OutputStream outputStream) {
        try (ZipOutputStream zipOutPut = new ZipOutputStream(new BufferedOutputStream(outputStream, 4096))) {
            for (EnvironmentDto env : embeddedEnvironmentApi.listEnvironments()) {
                zipOutPut.putNextEntry(new ZipEntry(env.name + ".json"));
                om.writeValue(zipOutPut, env);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    @Override
    public String name() {
        return "environments";
    }
}
