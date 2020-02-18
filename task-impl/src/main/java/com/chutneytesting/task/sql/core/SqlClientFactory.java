package com.chutneytesting.task.sql.core;


import com.chutneytesting.task.spi.injectable.Target;

public interface SqlClientFactory {

    SqlClient create(Target target);

}
