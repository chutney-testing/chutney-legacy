package com.chutneytesting.admin.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.chutneytesting.admin.api.GitRepositoryAdminController.GitRepositoryDto;
import com.chutneytesting.admin.infra.storage.JsonFilesGitRepository;
import com.chutneytesting.design.infra.storage.scenario.git.GitRepository;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class GitRepositoryAdminControllerTest {

    private JsonFilesGitRepository jsonFilesGitRepository = mock(JsonFilesGitRepository.class);

    private MockMvc mockMvc;

    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();

    @Before
    public void setUp() {
        reset(jsonFilesGitRepository);
        GitRepositoryAdminController gitRepositoryAdminController = new GitRepositoryAdminController(jsonFilesGitRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(gitRepositoryAdminController).build();
    }

    @Test
    public void should_list_all_existing_git_repositories() throws Exception {
        //Given
        Set<GitRepository> repos = new HashSet<>();
        repos.add(new GitRepository(1L, "url", "subFolder", "repoName"));
        when(jsonFilesGitRepository.listGitRepository()).thenReturn(repos);

        // When and then
        String contentAsString = mockMvc.perform(MockMvcRequestBuilders.get("/api/source/git/v1")
            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn().getResponse().getContentAsString();

        GitRepositoryDto[] gitRepositoryDtos = om.readValue(contentAsString, GitRepositoryDto[].class);
        assertThat(gitRepositoryDtos).hasSize(1);
        assertThat(gitRepositoryDtos[0].url).isEqualTo("url");
        assertThat(gitRepositoryDtos[0].name).isEqualTo("repoName");
        assertThat(gitRepositoryDtos[0].sourceDirectory).isEqualTo("subFolder");
    }

    @Test
    public void should_add_new_repository() throws Exception {

        GitRepositoryDto gitRepoDto = new GitRepositoryDto(null, "url", "repoName", "subFolder");

        // When and then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/source/git/v1")
            .content(om.writeValueAsString(gitRepoDto))
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isOk());

        ArgumentCaptor<GitRepository> argumentCaptor = ArgumentCaptor.forClass(GitRepository.class);
        verify(jsonFilesGitRepository, times(1)).save(argumentCaptor.capture());
        GitRepository captorValue = argumentCaptor.getValue();
        assertThat(captorValue.id).isEqualTo(1L);
        assertThat(captorValue.url).isEqualTo("url");
        assertThat(captorValue.testSubFolder).isEqualTo("subFolder");
        assertThat(captorValue.repositoryName).isEqualTo("repoName");

    }

    @Test
    public void should_update_existing_repository() throws Exception {
        //Given
        Set<GitRepository> repos = new HashSet<>();
        repos.add(new GitRepository(8L, "url", "subFolder", "repoName"));
        when(jsonFilesGitRepository.listGitRepository()).thenReturn(repos);

        GitRepositoryDto gitRepoDto = new GitRepositoryDto(8L, "url2", "repoName2", "subFolder2");

        // When and then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/source/git/v1")
            .content(om.writeValueAsString(gitRepoDto))
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isOk());

        ArgumentCaptor<GitRepository> argumentCaptor = ArgumentCaptor.forClass(GitRepository.class);
        verify(jsonFilesGitRepository, times(1)).save(argumentCaptor.capture());
        GitRepository captorValue = argumentCaptor.getValue();

        assertThat(captorValue.id).isEqualTo(8L);
        assertThat(captorValue.url).isEqualTo("url2");
        assertThat(captorValue.testSubFolder).isEqualTo("subFolder2");
        assertThat(captorValue.repositoryName).isEqualTo("repoName2");
    }
}
