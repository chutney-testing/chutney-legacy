package com.chutneytesting.task.radius;


import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.util.SocketUtils.findAvailableTcpPort;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Target;
import java.net.InetSocketAddress;
import java.util.Random;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.util.SocketUtils;
import org.tinyradius.util.RadiusServer;

class RadiusTasksTest {

    private static RadiusServer server;
    private static int authPort;
    private static int accPort;
    private static Target testTarget;

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

        };
        Integer[] httpPortRange = getRandomPortRange(100);
        authPort = findAvailableTcpPort(httpPortRange[0], httpPortRange[1]);
        httpPortRange = getRandomPortRange(100);
        accPort = findAvailableTcpPort(httpPortRange[0], httpPortRange[1]);

        server.setAuthPort(authPort);
        server.setAcctPort(accPort);

        testTarget = TestTarget.TestTargetBuilder.builder()
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

    @Test
    public void authenticate_should_success_with_valid_input() {
        Task sut = new RadiusAuthenticateTask(new TestLogger(), testTarget, "userName", "password", null, null);

        TaskExecutionResult result = sut.execute();

        assertThat(result.status).isEqualTo(Success);
    }

    @Test
    public void accounting_should_success_with_valid_input() {
        Task sut = new RadiusAccountingTask(new TestLogger(), testTarget, "userName", null, 1);

        TaskExecutionResult result = sut.execute();

        assertThat(result.status).isEqualTo(Success);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("parametersForAuthenticate_should_failed_on_wrong_input")
    public void authenticate_should_failed_on_wrong_input(String title, Target target, String userName, String password, Class exceptionType) {
        assertThatExceptionOfType(exceptionType)
            .isThrownBy(() -> {
                new RadiusAuthenticateTask(new TestLogger(), target, userName, password, null, null);
            });
    }


    public static Object[] parametersForAuthenticate_should_failed_on_wrong_input() {
        TestTarget noUrl = TestTarget.TestTargetBuilder.builder()
            .withProperty("sharedSecret", "secret")
            .withProperty("authenticatePort", valueOf(authPort))
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noSecret = TestTarget.TestTargetBuilder.builder()
            .withUrl("tcp://localhost:12345")
            .withProperty("authenticatePort", valueOf(authPort))
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noAuthenticatePort = TestTarget.TestTargetBuilder.builder()
            .withUrl("tcp://localhost:12345")
            .withProperty("sharedSecret", "secret")
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noAccountingPort = TestTarget.TestTargetBuilder.builder()
            .withUrl("tcp://localhost:12345")
            .withProperty("sharedSecret", "secret")
            .withProperty("authenticatePort", valueOf(authPort))
            .build();
        return new Object[]{
            new Object[]{"No target", null, "userName", "password", NullPointerException.class, "Please provide a target"},
            new Object[]{"No userName", testTarget, "", "password", IllegalArgumentException.class, "Please set userName"},
            new Object[]{"No password", testTarget, "userName", "", IllegalArgumentException.class, "Please set userPassword"},
            new Object[]{"No url", noUrl, "userName", "password", NullPointerException.class, "Please set url on target"},
            new Object[]{"No secret", noSecret, "userName", "password", NullPointerException.class, "Please set sharedSecret properties on target"},
            new Object[]{"No authenticatePort", noAuthenticatePort, "userName", "password", NullPointerException.class, "Please set authenticatePort properties on target"},
            new Object[]{"No accountingPort", noAccountingPort, "userName", "password", NullPointerException.class, "Please set accountingPort properties on target"}
        };
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parametersForAccounting_should_failed_on_wrong_input")
    public void accounting_should_failed_on_wrong_input(String title, Target target, String userName, Integer accountingType, Class exceptionType, String message) {
        assertThatExceptionOfType(exceptionType)
            .isThrownBy(() -> {
                new RadiusAccountingTask(new TestLogger(), target, userName, null, accountingType);
            })
            .withMessage(message);
    }

    public static Object[] parametersForAccounting_should_failed_on_wrong_input() {
        TestTarget noUrl = TestTarget.TestTargetBuilder.builder()
            .withProperty("sharedSecret", "secret")
            .withProperty("authenticatePort", valueOf(authPort))
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noSecret = TestTarget.TestTargetBuilder.builder()
            .withUrl("tcp://localhost:12345")
            .withProperty("authenticatePort", valueOf(authPort))
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noAuthenticatePort = TestTarget.TestTargetBuilder.builder()
            .withUrl("tcp://localhost:12345")
            .withProperty("sharedSecret", "secret")
            .withProperty("accountingPort", valueOf(accPort))
            .build();
        TestTarget noAccountingPort = TestTarget.TestTargetBuilder.builder()
            .withUrl("tcp://localhost:12345")
            .withProperty("sharedSecret", "secret")
            .withProperty("authenticatePort", valueOf(authPort))
            .build();
        return new Object[]{
            new Object[]{"No target", null, "userName", 1, NullPointerException.class, "Please provide a target"},
            new Object[]{"No userName", testTarget, "", 1, IllegalArgumentException.class, "Please set userName"},
            new Object[]{"No accountingType", testTarget, "userName", null, NullPointerException.class, "Please set accountingType (by default start = 1, stop = 2, interim = 3, on = 7, off = 8)"},
            new Object[]{"No url", noUrl, "userName", 1, NullPointerException.class, "Please set url on target"},
            new Object[]{"No secret", noSecret, "userName", 1, NullPointerException.class, "Please set sharedSecret properties on target"},
            new Object[]{"No authenticatePort", noAuthenticatePort, "userName", 1, NullPointerException.class, "Please set authenticatePort properties on target"},
            new Object[]{"No accountingPort", noAccountingPort, "userName", 1, NullPointerException.class, "Please set accountingPort properties on target"}
        };
    }

    private static Integer[] getRandomPortRange(int maxRangesNumber) {
        int range = (SocketUtils.PORT_RANGE_MAX - SocketUtils.PORT_RANGE_MIN) / maxRangesNumber;
        int rangeMin = SocketUtils.PORT_RANGE_MIN + (new Random().nextInt(maxRangesNumber) * range);
        return new Integer[]{rangeMin, rangeMin + range};
    }
}
