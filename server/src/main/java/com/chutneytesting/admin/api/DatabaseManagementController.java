package com.chutneytesting.admin.api;

import com.chutneytesting.server.core.domain.admin.DatabaseAdminService;
import com.chutneytesting.server.core.domain.admin.SqlResult;
import com.chutneytesting.server.core.domain.tools.PaginatedDto;
import com.chutneytesting.server.core.domain.tools.PaginationRequestWrapperDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/database")
@CrossOrigin(origins = "*")
public class DatabaseManagementController {

    private final DatabaseAdminService jdbcAdminService;

    DatabaseManagementController(DatabaseAdminService jdbcAdminService) {
        this.jdbcAdminService = jdbcAdminService;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping("/execute/jdbc")
    public SqlResult executeh2(@RequestBody String query) {
        return jdbcAdminService.execute(query);
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping("/paginate/jdbc")
    public PaginatedDto<SqlResult> executeh2(@RequestBody PaginationRequestWrapperDto<String> paginationRequestWrapperDto) {
        return jdbcAdminService.paginate(paginationRequestWrapperDto);
    }
}
