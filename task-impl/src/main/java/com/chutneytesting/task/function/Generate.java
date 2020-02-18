package com.chutneytesting.task.function;

import com.mifmif.common.regex.Generex;

public class Generate {
    private static final Generex UUID_GENERATOR = new Generex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    public String uuid() {
        return UUID_GENERATOR.random();
    }
}
