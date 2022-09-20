package com.chutneytesting.agent.domain.explore;

public class UndefinedPortException extends RuntimeException {

    public UndefinedPortException(String url, String protocol) {
        super("Port is not defined on [" + url + "]. Cannot default port for [" + protocol +"] protocol.");
    }

}
