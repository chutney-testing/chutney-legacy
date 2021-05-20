package com.chutneytesting.admin;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.admin.domain.gitbackup.GitBackupService;
import com.chutneytesting.admin.domain.gitbackup.GitClient;
import com.chutneytesting.admin.domain.gitbackup.Remotes;
import com.chutneytesting.tools.ui.MyMixInForIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class GitSpringConfiguration {

    @Bean
    public ObjectMapper gitObjectMapper() {
        return new ObjectMapper()
            .addMixIn(Resource.class, MyMixInForIgnoreType.class)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .findAndRegisterModules();
    }

    @Bean
    public GitBackupService gitBackupService(Remotes remotes,
                                             GitClient gitClient,
                                             Set<ChutneyContentProvider> contentProviders,
                                             @Value("${chutney.configuration-folder:~/.chutney/conf}") String gitRepositoryFolderPath) {
        return new GitBackupService(remotes, gitClient, contentProviders, gitRepositoryFolderPath);
    }

}
