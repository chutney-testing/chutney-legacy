package blackbox;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import java.io.IOException;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.util.SocketUtils;

@RunWith(Cucumber.class)
@CucumberOptions(strict = true, plugin = {"pretty", "json:target/cucumber-report.json"},
    features = {"classpath:blackbox/"}, tags = {"@DataSet"})
public class CucumberIT {

    private static TemporaryFolder tmpFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws IOException {
        int port = findAvailableTcpPort();
        System.setProperty("port", String.valueOf(port));
        int securePort = findAvailableTcpPort();
        System.setProperty("securePort", String.valueOf(securePort));
        tmpFolder.create();
        String tmpConfDir = tmpFolder.newFolder("conf").getAbsolutePath();
        System.setProperty("configuration-folder", tmpConfDir);
        System.setProperty("persistence-repository-folder", tmpConfDir);
    }

    @AfterClass
    public static void tearDown() {
        tmpFolder.delete();
    }

    private static int findAvailableTcpPort() {
        Integer[] httpPortRange = getRandomPortRange(100);
        return SocketUtils.findAvailableTcpPort(httpPortRange[0], httpPortRange[1]);
    }

    private static Integer[] getRandomPortRange(int maxRangesNumber) {
        int range = (SocketUtils.PORT_RANGE_MAX - SocketUtils.PORT_RANGE_MIN) / maxRangesNumber;
        int rangeMin = SocketUtils.PORT_RANGE_MIN + (new Random().nextInt(maxRangesNumber) * range);
        return new Integer[] {rangeMin, rangeMin + range};
    }
}
