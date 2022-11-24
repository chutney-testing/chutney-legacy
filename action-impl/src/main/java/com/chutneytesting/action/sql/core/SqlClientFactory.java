package com.chutneytesting.action.sql.core;


import com.chutneytesting.action.spi.injectable.Target;

public interface SqlClientFactory {

    SqlClient create(Target target);

}
