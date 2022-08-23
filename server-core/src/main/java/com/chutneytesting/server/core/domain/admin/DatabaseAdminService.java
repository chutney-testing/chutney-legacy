package com.chutneytesting.server.core.domain.admin;

import com.chutneytesting.server.core.domain.tools.PaginatedDto;
import com.chutneytesting.server.core.domain.tools.PaginationRequestWrapperDto;

public interface DatabaseAdminService {

    SqlResult execute(String statement);
    PaginatedDto<SqlResult> paginate(PaginationRequestWrapperDto<String> paginationRequestWrapperDto);
}
