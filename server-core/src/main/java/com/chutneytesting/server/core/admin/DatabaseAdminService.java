package com.chutneytesting.server.core.admin;

import com.chutneytesting.server.core.tools.PaginatedDto;
import com.chutneytesting.server.core.tools.PaginationRequestWrapperDto;

public interface DatabaseAdminService {

    SqlResult execute(String statement);
    PaginatedDto<SqlResult> paginate(PaginationRequestWrapperDto<String> paginationRequestWrapperDto);
}
