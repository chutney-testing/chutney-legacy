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

package com.chutneytesting.admin.api;

import com.chutneytesting.admin.domain.DBVacuum;
import com.chutneytesting.execution.api.ExecutionSummaryDto;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/database")
@CrossOrigin(origins = "*")
public class DatabaseManagementController {

    private final ExecutionHistoryRepository executionHistoryRepository;
    private final DBVacuum dbVacuum;

    DatabaseManagementController(
        ExecutionHistoryRepository executionHistoryRepository,
        DBVacuum dbVacuum
    ) {
        this.executionHistoryRepository = executionHistoryRepository;
        this.dbVacuum = dbVacuum;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = "/execution", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ExecutionSummaryDto> getExecutionReportMatchQuery(@QueryParam("query") String query) {
        return executionHistoryRepository.getExecutionReportMatchQuery(query).stream().map(ExecutionSummaryDto::toDto).toList();
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping(path = "/compact")
    public void vacuum() {
        dbVacuum.vacuum();
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = "/size", produces = MediaType.APPLICATION_JSON_VALUE)
    public Long dbSize() {
        return dbVacuum.size();
    }
}
