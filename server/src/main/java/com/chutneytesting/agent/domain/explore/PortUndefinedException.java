package com.chutneytesting.agent.domain.explore;

public class PortUndefinedException extends RuntimeException {

    public PortUndefinedException(String url, String protocol) {
        super("Port is not defined on [" + url + "]. Cannot default port for [" + protocol +"] protocol.");
    }

}
