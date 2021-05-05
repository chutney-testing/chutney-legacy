package blackbox;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.SecurityInfo;
import com.chutneytesting.environment.domain.Target;
import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.junit.api.AfterAll;
import com.chutneytesting.junit.api.BeforeAll;
import com.chutneytesting.junit.api.Chutney;
import com.chutneytesting.junit.api.EnvironmentService;
import com.chutneytesting.security.domain.Authorization;
import com.chutneytesting.security.domain.Authorizations;
import com.chutneytesting.security.domain.Role;
import com.chutneytesting.security.domain.User;
import com.chutneytesting.security.domain.UserRoles;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.groovy.util.Maps;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.SocketUtils;

@Chutney
public class ChutneyTest {

    private ConfigurableApplicationContext localChutney;
    private final int port;
    private final int securePort;
    private final int dbPort;
    private final Path tmpFolder;

    private final EnvironmentService environmentService;
    private static final String TEST_ENV_NAME = "ENV";

    public ChutneyTest(EnvironmentService environmentService) throws IOException {
        this.environmentService = environmentService;

        port = findAvailableTcpPort();
        securePort = findAvailableTcpPort();
        dbPort = findAvailableTcpPort();

        tmpFolder = Files.createTempDirectory("chutney");
        tmpFolder.toFile().createNewFile();

        setEnvironment();
    }

    @BeforeAll
    public void setUp() {
        Path tmpConfDir = tmpFolder.resolve("conf");

        System.setProperty("port", String.valueOf(port));
        System.setProperty("securePort", String.valueOf(securePort));
        System.setProperty("chutney.db-server.port", String.valueOf(dbPort));
        System.setProperty("configuration-folder", tmpConfDir.toString());

        localChutney = SpringApplication.run(IntegrationTestConfiguration.class);

        initAuthorizations();
    }

    @AfterAll
    public void tearDown() {
        tmpFolder.toFile().delete();
        localChutney.stop();
        cleanEnvironment();
    }

    private static int findAvailableTcpPort() {
        Integer[] httpPortRange = getRandomPortRange(100);
        return SocketUtils.findAvailableTcpPort(httpPortRange[0], httpPortRange[1]);
    }

    private static Integer[] getRandomPortRange(int maxRangesNumber) {
        int range = (SocketUtils.PORT_RANGE_MAX - SocketUtils.PORT_RANGE_MIN) / maxRangesNumber;
        int rangeMin = SocketUtils.PORT_RANGE_MIN + (new Random().nextInt(maxRangesNumber) * range);
        return new Integer[]{rangeMin, rangeMin + range};
    }


    private void setEnvironment() {
        try {
            initEnvironment();
        } catch (AlreadyExistingEnvironmentException aeee) {
            cleanEnvironment();
            initEnvironment();
        }

        addChutneyLocalServer();
        addChutneyDBServer();
    }

    private void addChutneyLocalServer() {
        environmentService.addTarget(TEST_ENV_NAME,
            TargetDto.from(Target.builder()
                .withName("CHUTNEY_LOCAL")
                .withEnvironment(TEST_ENV_NAME)
                .withUrl("https://localhost:" + securePort)
                .withSecurity(
                    SecurityInfo.builder()
                        .credential(SecurityInfo.Credential.of("admin", "admin"))
                        .build()
                )
                .build())
        );

        environmentService.addTarget(TEST_ENV_NAME,
            TargetDto.from(Target.builder()
                .withName("CHUTNEY_LOCAL_NO_USER")
                .withEnvironment(TEST_ENV_NAME)
                .withUrl("https://localhost:" + securePort)
                .build())
        );
    }

    private void addChutneyDBServer() {
        Target dbTarget;
        String spring_profiles_active = System.getenv("SPRING_PROFILES_ACTIVE");
        if (spring_profiles_active != null && spring_profiles_active.contains("db-pg")) { // Check H2 or Postgres
            dbTarget = Target.builder()
                .withName("CHUTNEY_DB")
                .withEnvironment(TEST_ENV_NAME)
                .withUrl("tcp://localhost:" + dbPort)
                .withProperties(
                    Maps.of(
                        "driverClassName", "org.postgresql.Driver",
                        "jdbcUrl", "jdbc:postgresql://localhost:" + dbPort + "/postgres",
                        "dataSource.user", "postgres",
                        "dataSource.password", "postgres",
                        "maximumPoolSize", "2"
                    )
                )
                .build();
        } else { // H2 by default
            dbTarget = Target.builder()
                .withName("CHUTNEY_DB")
                .withEnvironment(TEST_ENV_NAME)
                .withUrl("tcp://localhost:" + dbPort)
                .withProperties(
                    Maps.of(
                        "driverClassName", "org.h2.Driver",
                        "jdbcUrl", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                        "dataSource.user", "sa",
                        "dataSource.password", "",
                        "maximumPoolSize", "2"
                    )
                )
                .build();
        }

        environmentService.addTarget(ChutneyTest.TEST_ENV_NAME, TargetDto.from(dbTarget));
    }

    private void initEnvironment() {
        environmentService.addEnvironment(EnvironmentDto.from(Environment.builder().withName(TEST_ENV_NAME).build()));
    }

    private void cleanEnvironment() {
        environmentService.deleteEnvironment(TEST_ENV_NAME);
    }

    /**
     * Declare roles and users roles.
     * Users are declared in application-users.yml
     */
    private void initAuthorizations() {
        Authorizations auth = localChutney.getBean(Authorizations.class);

        UserRoles userRoles = UserRoles.builder()
            .withRoles(List.of(
                Role.DEFAULT,
                Role.builder().withName("TESTCASE_EDITOR").withAuthorizations(List.of(Authorization.SCENARIO_WRITE.name())).build(),
                Role.builder().withName("GOD").withAuthorizations(Arrays.stream(Authorization.values()).map(Enum::name).collect(toList())).build()
            ))
            .withUsers(List.of(
                User.builder().withId("admin").withRole("GOD").build(),
                User.builder().withId("robert").withRole("TESTCASE_EDITOR").build(),
                User.builder().withId("paloma").withRole("TESTCASE_EDITOR").build()
            ))
            .build();

        auth.save(userRoles);
    }
}
