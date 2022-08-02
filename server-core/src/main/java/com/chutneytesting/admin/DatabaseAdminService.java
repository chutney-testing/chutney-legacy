package com.chutneytesting.admin;

import com.chutneytesting.tools.PaginatedDto;
import com.chutneytesting.tools.PaginationRequestWrapperDto;

public interface DatabaseAdminService {

    SqlResult execute(String statement);
    PaginatedDto<SqlResult> paginate(PaginationRequestWrapperDto<String> paginationRequestWrapperDto);
}
