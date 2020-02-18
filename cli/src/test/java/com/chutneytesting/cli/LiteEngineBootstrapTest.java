package com.chutneytesting.cli;

import org.junit.Test;

public class LiteEngineBootstrapTest {

    @Test
    public void start() {
        String examplePath = LiteEngineBootstrapTest.class.getClassLoader().getResource("example.json").getPath();
        String envPath = LiteEngineBootstrapTest.class.getClassLoader().getResource("GLOBAL.json").getPath();
        LiteEngineBootstrap.main("--env", envPath, examplePath);
    }

    @Test
    public void start_async() {
        String examplePath = LiteEngineBootstrapTest.class.getClassLoader().getResource("example.json").getPath();
        String envPath = LiteEngineBootstrapTest.class.getClassLoader().getResource("GLOBAL.json").getPath();
        LiteEngineBootstrapRx.main("--env", envPath, examplePath);
    }
}
