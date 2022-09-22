package com.chutneytesting.task.function;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NetworkFunctionsTest {

    @Test
    void randomNetworkMask() {
        assertThat(NetworkFunctions.randomNetworkMask()).matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    }

    @Test
    void hostIpMatching() throws Exception {
        assertThat(NetworkFunctions.hostIpMatching("127.0.*")).matches("127.0.0.1");
    }
}
