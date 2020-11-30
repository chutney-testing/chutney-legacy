package blackbox;

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
        @Value("${chutney.db-server.port}") int dbServerPort,
        @Value("${chutney.db-server.base-dir:~/.chutney/pgdata}") String baseDir,
        @Value("${chutney.db-server.work-dir:~/.chutney/pgwork}") String workDir) throws IOException {

        return EmbeddedPostgres.builder()
            .setPort(dbServerPort)
            .setDataDirectory(baseDir)
            .setCleanDataDirectory(true)
            .setOverrideWorkingDirectory(new File(workDir))
            .start();
    }
}
