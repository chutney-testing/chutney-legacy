package blackbox;

import com.chutneytesting.design.domain.environment.Environment;
import com.chutneytesting.design.domain.environment.Target;
import com.chutneytesting.junit.api.AfterAll;
import com.chutneytesting.junit.api.BeforeAll;
import com.chutneytesting.junit.api.Chutney;
import com.chutneytesting.junit.engine.EnvironmentService;
import java.io.IOException;
import java.util.Random;
import org.apache.groovy.util.Maps;
import org.junit.rules.TemporaryFolder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.SocketUtils;

@Chutney
public class ChutneyIT {

    private ConfigurableApplicationContext localChutney;
    private int port;
    private int securePort;
    private int dbPort;
    private TemporaryFolder tmpFolder;

    private EnvironmentService environmentService;

    public ChutneyIT(EnvironmentService environmentService) throws IOException {
        this.environmentService = environmentService;

        port = findAvailableTcpPort();
        securePort = findAvailableTcpPort();
        dbPort = findAvailableTcpPort();

        tmpFolder = new TemporaryFolder();
        tmpFolder.create();

        setEnvironment(securePort);
    }

    @BeforeAll
    public void setUp() throws IOException {
        String tmpConfDir = tmpFolder.newFolder("conf").getAbsolutePath();

        System.setProperty("port", String.valueOf(port));
        System.setProperty("securePort", String.valueOf(securePort));
        System.setProperty("chutney.db-server.port", String.valueOf(dbPort));
        System.setProperty("configuration-folder", tmpConfDir);
        System.setProperty("persistence-repository-folder", tmpConfDir);

        localChutney = SpringApplication.run(IntegrationTestConfiguration.class);
    }

    @AfterAll
    public void tearDown() {
        tmpFolder.delete();
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

    private void setEnvironment(int securePort) {
        String envName = "ENV";

        environmentService.addEnvironment(Environment.builder().withName(envName).build());

        addChutneyLocalServer(envName);
        addChutneyDBServer(envName);
    }

    private void addChutneyLocalServer(String envName) {
        environmentService.addTarget(envName,
            Target.builder()
                .withId(Target.TargetId.of("CHUTNEY_LOCAL", envName))
                .withUrl("https://localhost:" + securePort)
                .build()
        );
    }

    private void addChutneyDBServer(String envName) {
        Target dbTarget;
        String spring_profiles_active = System.getenv("SPRING_PROFILES_ACTIVE");
        if (spring_profiles_active != null && spring_profiles_active.contains("db-pg")) { // Check H2 or Postgres
            dbTarget = Target.builder()
                .withId(Target.TargetId.of("CHUTNEY_DB", envName))
                .withUrl("tcp://localhost:" + dbPort)
                .withProperties(
                    Maps.of(
                        "driverClassName", "org.postgresql.Driver",
                        "jdbcUrl", "jdbc:postgresql://localhost:"+dbPort+"/postgres",
                        "dataSource.user", "postgres",
                        "dataSource.password", "postgres",
                        "maximumPoolSize", "2"
                    )
                )
                .build();
        } else { // H2 by default
            dbTarget = Target.builder()
                .withId(Target.TargetId.of("CHUTNEY_DB", envName))
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

        environmentService.addTarget(envName, dbTarget);
    }

    private void cleanEnvironment() {
        environmentService.deleteEnvironment("ENV");
    }
}
