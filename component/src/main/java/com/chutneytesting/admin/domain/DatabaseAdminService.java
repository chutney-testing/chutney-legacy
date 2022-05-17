package com.chutneytesting.admin.domain;

import com.chutneytesting.tools.PaginatedDto;
import com.chutneytesting.tools.PaginationRequestWrapperDto;

public interface DatabaseAdminService {

    SqlResult execute(String statement);
    PaginatedDto<SqlResult> paginate(PaginationRequestWrapperDto<String> paginationRequestWrapperDto);
}
