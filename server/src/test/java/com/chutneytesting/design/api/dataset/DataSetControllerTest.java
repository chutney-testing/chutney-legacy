package com.chutneytesting.design.api.dataset;

import static com.chutneytesting.design.api.dataset.DataSetMapper.fromDto;
import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.ui.ComposableIdUtils.toFrontId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.RestExceptionHandler;
import com.chutneytesting.WebConfiguration;
import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetHistoryRepository;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(JUnitParamsRunner.class)
public class DataSetControllerTest {

    @Rule
    public MethodRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DataSetRepository dataSetRepository;
    @Mock
    private DataSetHistoryRepository dataSetHistoryRepository;

    @InjectMocks
    private DataSetController sut;

    private MockMvc mockMvc;

    private final ObjectMapper om = new WebConfiguration().objectMapper();

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sut)
            .setControllerAdvice(new RestExceptionHandler())
            .build();
    }

    @Test
    public void should_findAll_datasets_sorted_by_name() throws Exception {
        // Given
        String firstDataSetFrontId = "10-2";
        String secondDataSetFrontId = "10-3";
        String thirdDataSetFrontId = "10-4";
        Integer firstDataSetVersion = 1;
        Integer secondDataSetVersion = 9;
        Integer thirdDataSetVersion = 4;
        Pair<DataSet, DataSetDto> firstDataSet = dataSetMetaData(firstDataSetFrontId, "a name", firstDataSetVersion);
        Pair<DataSet, DataSetDto> secondDataSet = dataSetMetaData(secondDataSetFrontId, "c name", secondDataSetVersion);
        Pair<DataSet, DataSetDto> thirdDataSet = dataSetMetaData(thirdDataSetFrontId, "b name", thirdDataSetVersion);
        when(dataSetRepository.findAll())
            .thenReturn(
                Lists.list(firstDataSet.getLeft(), secondDataSet.getLeft(), thirdDataSet.getLeft()));
        when(dataSetHistoryRepository.lastVersion(eq(fromFrontId(firstDataSetFrontId)))).thenReturn(firstDataSetVersion);
        when(dataSetHistoryRepository.lastVersion(eq(fromFrontId(secondDataSetFrontId)))).thenReturn(secondDataSetVersion);
        when(dataSetHistoryRepository.lastVersion(eq(fromFrontId(thirdDataSetFrontId)))).thenReturn(thirdDataSetVersion);

        // When
        List<DataSetDto> dtos = new ArrayList<>();
        mockMvc.perform(get(DataSetController.BASE_URL))
            .andDo(result -> dtos.addAll(om.readValue(result.getResponse().getContentAsString(), new TypeReference<List<DataSetDto>>() {
            })))
            .andExpect(status().isOk());

        // Then
        assertThat(dtos).containsExactly(firstDataSet.getRight(), thirdDataSet.getRight(), secondDataSet.getRight());
    }

    @Test
    public void should_save_new_dataset() throws Exception {
        // Given
        String newId = "#1:9";
        DataSetDto dataSetDto = dataSetMetaData(null, "name", 0).getRight();
        when(dataSetRepository.save(any())).thenReturn(newId);
        when(dataSetHistoryRepository.addVersion(any())).thenReturn(Optional.of(Pair.of("#2:6", 1)));

        // When
        MvcResult mvcResult = mockMvc.perform(
            post(DataSetController.BASE_URL)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(om.writeValueAsString(dataSetDto)))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        DataSetDto savedDataSetDto = om.readValue(mvcResult.getResponse().getContentAsString(), DataSetDto.class);
        assertThat(savedDataSetDto).isEqualTo(ImmutableDataSetDto.builder().from(dataSetDto).id(toFrontId(newId)).version(1).build());
    }

    @Test
    public void should_update_dataset() throws Exception {
        // Given
        String id = "1-9";
        Pair<DataSet, DataSetDto> dataSet = dataSetMetaData(id, "name", 1);
        DataSet oldDataSet = DataSet.builder().fromDataSet(dataSet.getLeft()).build();
        when(dataSetRepository.findById(eq(fromFrontId(id)))).thenReturn(oldDataSet);
        when(dataSetRepository.save(any())).thenReturn(fromFrontId(id));
        when(dataSetHistoryRepository.addVersion(any())).thenReturn(Optional.of(Pair.of("#2:6", 2)));

        // When
        MvcResult mvcResult = mockMvc.perform(
            post(DataSetController.BASE_URL)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(om.writeValueAsString(dataSet.getRight())))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        DataSetDto savedDataSetDto = om.readValue(mvcResult.getResponse().getContentAsString(), DataSetDto.class);
        assertThat(savedDataSetDto).isEqualTo(ImmutableDataSetDto.builder().from(dataSet.getRight()).version(2).build());
    }

    @Test
    public void should_remove_dataset_by_id() throws Exception {
        // Given
        String id = "1-5";

        // When
        MvcResult mvcResult = mockMvc.perform(
            delete(DataSetController.BASE_URL + "/" + id))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        verify(dataSetRepository).removeById(eq(fromFrontId(id)));
        verify(dataSetHistoryRepository).removeHistory(eq(fromFrontId(id)));
        assertThat(mvcResult.getResponse().getContentLength()).isZero();
    }

    @Test
    public void should_find_dataset_by_id() throws Exception {
        // Given
        String id = "1-5";
        int version = 1;
        Pair<DataSet, DataSetDto> dataSet = dataSetMetaData(id, "name", version);
        when(dataSetRepository.findById(eq(fromFrontId(id))))
            .thenReturn(DataSet.builder().fromDataSet(dataSet.getLeft()).withId(fromFrontId(id)).build());
        when(dataSetHistoryRepository.lastVersion(eq(fromFrontId(id)))).thenReturn(version);

        // When
        MvcResult mvcResult = mockMvc.perform(
            get(DataSetController.BASE_URL + "/" + id))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        DataSetDto dto = om.readValue(mvcResult.getResponse().getContentAsString(), DataSetDto.class);
        assertThat(dto).isEqualTo(dataSet.getRight());
    }

    @Test
    public void should_find_dataset_last_version_number() throws Exception {
        // Given
        String id = "1-5";
        when(dataSetHistoryRepository.lastVersion(eq(fromFrontId(id)))).thenReturn(5);

        // When
        MvcResult mvcResult = mockMvc.perform(
            get(DataSetController.BASE_URL + "/" + id + "/versions/last"))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("5");
    }

    @Test
    public void should_get_all_dataset_versions() throws Exception {
        // Given
        String id = "1-5";
        Pair<DataSet, DataSetDto> one = dataSetMetaData(id, "one", 2);
        Pair<DataSet, DataSetDto> two = dataSetMetaData(id, "two", 3);
        Pair<DataSet, DataSetDto> three = dataSetMetaData(id, "three", 4);
        when(dataSetHistoryRepository.allVersions(eq(fromFrontId(id))))
            .thenReturn(Maps.of(
                one.getRight().version(), one.getLeft(),
                two.getRight().version(), two.getLeft(),
                three.getRight().version(), three.getLeft()
            ));

        // When
        MvcResult mvcResult = mockMvc.perform(
            get(DataSetController.BASE_URL + "/" + id + "/versions"))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        List<DataSetDto> allVersions = om.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<DataSetDto>>() {
        });
        assertThat(allVersions)
            .containsExactlyElementsOf(Lists.list(one.getRight(), two.getRight(), three.getRight()));
    }

    @Test
    @Parameters({
        DataSetController.BASE_URL + "/%s/%s", DataSetController.BASE_URL + "/%s/versions/%s"
    })
    public void should_find_dataset_version(String urlTemplate) throws Exception {
        // Given
        String id = "1-5";
        Integer version = 5;
        Pair<DataSet, DataSetDto> dataSet = dataSetMetaData(id, "name", version);
        when(dataSetHistoryRepository.version(eq(fromFrontId(id)), eq(version)))
            .thenReturn(dataSet.getLeft());

        // When
        MvcResult mvcResult = mockMvc.perform(
            get(String.format(urlTemplate, id, version)))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        DataSetDto dto = om.readValue(mvcResult.getResponse().getContentAsString(), DataSetDto.class);
        assertThat(dto).isEqualTo(dataSet.getRight());
    }

    private Pair<DataSet, DataSetDto> dataSetMetaData(String frontId, String name, Integer version) {
        Instant now = Instant.now();
        DataSetDto dataSetDto = ImmutableDataSetDto.builder()
            .id(Optional.ofNullable(frontId))
            .name(name)
            .version(version)
            .lastUpdated(now)
            .build();
        return Pair.of(fromDto(dataSetDto), dataSetDto);
    }
}
