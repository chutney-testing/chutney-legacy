package blackbox;

import static com.chutneytesting.ServerConfiguration.DBSERVER_PG_BASEDIR_SPRING_BASE_VALUE;
import static com.chutneytesting.ServerConfiguration.DBSERVER_PG_WORKDIR_SPRING_BASE_VALUE;
import static com.chutneytesting.ServerConfiguration.DBSERVER_PORT_SPRING_VALUE;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.IOException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("db-pg")
public class DBConfiguration {

    @Bean
    @DependsOn("dbServer")
    public DataSource dataSource(DataSourceProperties internalDataSourceProperties) {
        return internalDataSourceProperties.initializeDataSourceBuilder()
            .type(HikariDataSource.class).build();
    }

    @Bean
    EmbeddedPostgres dbServer(
        @Value(DBSERVER_PORT_SPRING_VALUE) int dbServerPort,
        @Value(DBSERVER_PG_BASEDIR_SPRING_BASE_VALUE) String baseDir,
        @Value(DBSERVER_PG_WORKDIR_SPRING_BASE_VALUE) String workDir) throws IOException {

        return EmbeddedPostgres.builder()
            .setPort(dbServerPort)
            .setDataDirectory(baseDir)
            .setCleanDataDirectory(true)
            .setOverrideWorkingDirectory(new File(workDir))
            .start();
    }
}
