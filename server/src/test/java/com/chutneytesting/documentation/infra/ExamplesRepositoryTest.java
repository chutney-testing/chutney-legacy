package com.chutneytesting.documentation.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.infra.storage.scenario.jdbc.TestCaseData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExamplesRepositoryTest {

    private Map<String, String> examples = new HashMap<>();

    @BeforeEach
    public void setUp() {
        examples.put("titre", "{\n" +
            "    scenario : {\n" +
            "        name : test\n" +
            "        steps : [\n" +
            "            {\n" +
            "                name : when something else\n" +
            "                type : success\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}\n");
    }

    @Test
    public void should_be_active_by_default() {
        ExamplesRepository examplesRepository = new ExamplesRepository(true, examples);
        assertThat(examplesRepository.isActive()).isEqualTo(true);
    }

    @Test
    public void should_be_inactive_when_toggle() {
        ExamplesRepository examplesRepository = new ExamplesRepository(true, examples);

        examplesRepository.toggleActivation();

        assertThat(examplesRepository.isActive()).isEqualTo(false);
    }

    @Test
    public void should_find_0_file_when_not_activated() {
        ExamplesRepository examplesRepository = new ExamplesRepository(true, examples);

        examplesRepository.toggleActivation();
        List<TestCaseMetadata> examples = examplesRepository.findAll();

        assertThat(examples).isEmpty();
    }

    @Test
    public void should_find_all_files_when_activated() {
        ExamplesRepository examplesRepository = new ExamplesRepository(true, examples);

        List<TestCaseMetadata> examples = examplesRepository.findAll();

        assertThat(examples.size()).isEqualTo(1);
    }

    @Test
    public void should_find_example_when_using_id() {
        ExamplesRepository examplesRepository = new ExamplesRepository(true, examples);

        Optional<TestCaseData> example = examplesRepository.findById(String.valueOf("titre".hashCode()));

        assertThat(example.isPresent()).isTrue();
    }

    @Test
    public void should_get_last_version() {
        ExamplesRepository examplesRepository = new ExamplesRepository(true, examples);

        Optional<Integer> version = examplesRepository.lastVersion(String.valueOf("titre".hashCode()));

        assertThat(version).hasValue(1);
    }
}
