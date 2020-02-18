package com.chutneytesting.design.infra.storage.scenario.git.json.versionned;

@SuppressWarnings("serial")
public class NoMapperForJsonVersionException extends RuntimeException {
    public NoMapperForJsonVersionException(String version) {
        super(version != null ? "no mapper for version " + version : "no mapper without version");
    }
}
