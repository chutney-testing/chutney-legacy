package com.chutneytesting.action.radius;


import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static com.chutneytesting.tools.SocketUtils.findAvailableTcpPort;
import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.SocketUtils;
import java.net.InetSocketAddress;
import java.util.Random;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusServer;

class RadiusActionsTest {

    private static RadiusServer server;
    private static int authPort;
    private static int accPort;
    private static Target testTarget;
    private static AccessRequest receivedAccessRequest;

    @BeforeAll
    public static void setUp() {
        server = new RadiusServer() {
            @Override
            public String getSharedSecret(InetSocketAddress client) {
                return "secret";
            }

            @Override
            public String getUserPassword(String userName) {
                return "password";
            }

            @Override
            public RadiusPacket accessRequestReceived(AccessRequest accessRequest, InetSocketAddress client) throws RadiusException {
                receivedAccessRequest = accessRequest;
                return super.accessRequestReceived(accessRequest, client);
            }
        };
        Integer[] httpPortRange = getRandomPortRange(100);
        authPort = findAvailableTcpPort(httpPortRange[0], httpPortRange[1]);
        httpPortRange = getRandomPortRange(100);
        accPort = findAvailableTcpPort(httpPortRange[0], httpPortRange[1]);

        server.setAuthPort(authPort);
        server.setAcctPort(accPort);

        testTarget = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target")
            .withUrl("tcp://localhost:12345")
            .withProperty("sharedSecret", "secret")
            .withProperty("authenticatePort", valueOf(authPort))
            .withProperty("accountingPort", valueOf(accPort))
            .build();

        server.start(true, true);

    }

    @AfterAll
    public static void tearDown() {
        server.stop();
    }

    @ParameterizedTest(name = "accounting_should_success_with_valid_input with protocol {0}")
    @ValueSource(strings = {"pap", "chap"})
    public void authenticate_should_success_with_valid_input(String protocol) {
        Action sut = new RadiusAuthenticateAction(new TestLogger(), testTarget, "userName", "password", protocol, null);

        ActionExecutionResult result = sut.execute();

        assertThat(result.status).isEqualTo(Success);
        assertThat(receivedAccessRequest.getAuthProtocol()).isEqualTo(protocol);
    }

    @Test
    public void accounting_should_success_with_valid_input() {
        Action sut = new RadiusAccountingAction(new TestLogger(), testTarget, "userName", null, 1);

        ActionExecutionResult result = sut.execute();

        assertThat(result.status).isEqualTo(Success);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parametersForAuthenticate_should_failed_on_wrong_input")
    public void authenticate_should_failed_on_wrong_input(String testName, Target target, String userName, String password, int expectedErrorsNb) {
        RadiusAuthenticateAction radiusAuthenticateAction = new RadiusAuthenticateAction(new TestLogger(), target, userName, password, null, null);
        assertThat(radiusAuthenticateAction.validateInputs())
            .as(testName)
            .hasSize(expectedErrorsNb);
    }

    public static Object[] parametersForAuthenticate_should_failed_on_wrong_input() {
        TestTarget noUrl = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target")
            .withProperty("sharedSecret", "secret")
            .withProperty("authenticatePort", valueOf(authPort))
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noSecret = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target")
            .withUrl("tcp://localhost:12345")
            .withProperty("authenticatePort", valueOf(authPort))
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noAuthenticatePort = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target")
            .withUrl("tcp://localhost:12345")
            .withProperty("sharedSecret", "secret")
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noAccountingPort = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target")
            .withUrl("tcp://localhost:12345")
            .withProperty("sharedSecret", "secret")
            .withProperty("authenticatePort", valueOf(authPort))
            .build();
        TestTarget invalidPorts = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target")
            .withUrl("tcp://localhost:12345")
            .withProperty("sharedSecret", "secret")
            .withProperty("authenticatePort", "authPort")
            .withProperty("accountingPort", valueOf(0))
            .build();
        return new Object[]{
            new Object[]{"No target", null, "userName", "password", 9},
            new Object[]{"No userName", testTarget, "", "password", 1},
            new Object[]{"No password", testTarget, "userName", "", 1},
            new Object[]{"No url", noUrl, "userName", "password", 2},
            new Object[]{"No secret", noSecret, "userName", "password", 1},
            new Object[]{"No authenticatePort", noAuthenticatePort, "userName", "password", 2},
            new Object[]{"No accountingPort", noAccountingPort, "userName", "password", 2},
            new Object[]{"InvalidPorts", invalidPorts, "userName", "password", 2}
        };
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parametersForAccounting_should_failed_on_wrong_input")
    public void accounting_should_failed_on_wrong_input(String testName, Target target, String userName, Integer accountingType, int expectedErrorsNb) {
        RadiusAccountingAction radiusAccountingAction = new RadiusAccountingAction(new TestLogger(), target, userName, null, accountingType);
        assertThat(radiusAccountingAction.validateInputs())
            .as(testName)
            .hasSize(expectedErrorsNb);
    }

    public static Object[] parametersForAccounting_should_failed_on_wrong_input() {
        TestTarget noUrl = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target")
            .withProperty("sharedSecret", "secret")
            .withProperty("authenticatePort", valueOf(authPort))
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noSecret = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target")
            .withUrl("tcp://localhost:12345")
            .withProperty("authenticatePort", valueOf(authPort))
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noAuthenticatePort = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target")
            .withUrl("tcp://localhost:12345")
            .withProperty("sharedSecret", "secret")
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noAccountingPort = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target")
            .withUrl("tcp://localhost:12345")
            .withProperty("sharedSecret", "secret")
            .withProperty("authenticatePort", valueOf(authPort))
            .build();
        TestTarget invalidPorts = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target")
            .withUrl("tcp://localhost:12345")
            .withProperty("sharedSecret", "secret")
            .withProperty("authenticatePort", "authPort")
            .withProperty("accountingPort", valueOf(0))
            .build();
        return new Object[]{
            new Object[]{"No target", null, "userName", 1, 9},
            new Object[]{"No userName", testTarget, "", 1, 1},
            new Object[]{"No accountingType", testTarget, "userName", null, 1},
            new Object[]{"Invalid accountingType", testTarget, "userName", 16, 1},
            new Object[]{"No url", noUrl, "userName", 1, 2},
            new Object[]{"No secret", noSecret, "userName", 1, 1},
            new Object[]{"No authenticatePort", noAuthenticatePort, "userName", 1, 2},
            new Object[]{"No accountingPort", noAccountingPort, "userName", 1, 2},
            new Object[]{"InvalidPorts", invalidPorts, "userName", 1, 2}
        };
    }

    private static Integer[] getRandomPortRange(int maxRangesNumber) {
        int range = (SocketUtils.PORT_RANGE_MAX - SocketUtils.PORT_RANGE_MIN) / maxRangesNumber;
        int rangeMin = SocketUtils.PORT_RANGE_MIN + (new Random().nextInt(maxRangesNumber) * range);
        return new Integer[]{rangeMin, rangeMin + range};
    }
}
