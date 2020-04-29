package blackbox.stepdef;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import blackbox.restclient.RestClient;
import com.chutneytesting.design.api.dataset.DataSetController;
import com.chutneytesting.design.api.dataset.DataSetDto;
import com.chutneytesting.design.api.dataset.ImmutableDataSetDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class DataSetStepDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetStepDefs.class);

    private final RestClient restClient;
    private final ObjectMapper om;

    private List<DataSetDto> savedDataSetDtos = new ArrayList<>();
    private List<DataSetDto> deletedDataSetDtos = new ArrayList<>();
    private DataSetDto foundDataSetDto;

    public DataSetStepDefs(RestClient restClient) {
        this.restClient = restClient;
        om = new ObjectMapper();
        om.registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    }

    @After
    public void after() {
        savedDataSetDtos.stream()
            .map(dto -> dto.id().get())
            .distinct()
            .forEach(dataSetId ->
                restClient.defaultRequest()
                    .withUrl(DataSetController.BASE_URL + "/" + dataSetId)
                    .delete());
        savedDataSetDtos.clear();
        deletedDataSetDtos.clear();
    }

    @Given("a dataset is saved")
    public void a_dataset_is_saved(String dataSetJson) throws Exception {
        DataSetDto dataSetDto = om.readValue(dataSetJson, DataSetDto.class);
        saveDataSet(dataSetDto);
    }

    @When("search for the dataset")
    public void search_for_the_dataset() {
        DataSetDto dataSetDto = lastSavedDataSetDto();
        findDataSet(dataSetDto.id().get(), null);
    }

    @Then("the dataset is retrieved")
    public void the_dataset_is_retrieved() {
        DataSetDto dataSetDto = lastSavedDataSetDto();
        assertThat(foundDataSetDto).isEqualTo(dataSetDto);
    }

    @When("delete the dataset")
    public void delete_the_dataset() {
        DataSetDto dataSetDto = lastSavedDataSetDto();
        deleteDataSet(dataSetDto);
    }

    @Then("the dataset cannot be found")
    public void the_dataset_cannot_be_found() {
        DataSetDto dataSetDto = deletedDataSetDtos.get(deletedDataSetDtos.size() - 1);
        noFindDataSet(dataSetDto.id().get());
    }

    @When("a new version is saved")
    public void a_new_version_is_saved(String dataSetJson) throws IOException {
        DataSetDto dataSetDto = om.readValue(dataSetJson, DataSetDto.class);
        dataSetDto = ImmutableDataSetDto.builder().from(dataSetDto).id(lastSavedDataSetDto().id()).build();
        saveDataSet(dataSetDto);
        assertThat(lastSavedDataSetDto().id()).isEqualTo(dataSetDto.id());
    }

    @Then("the dataset last version number is (.+)")
    public void the_dataset_last_version_number_is(Integer version) {
        DataSetDto dataSetDto = lastSavedDataSetDto();
        assertThat(dataSetLastVersionNumber(dataSetDto.id().get())).isEqualTo(version);
    }

    @Then("the list of version numbers is")
    public void the_list_of_version_numbers_is(List<Integer> versions) {
        DataSetDto dataSetDto = lastSavedDataSetDto();
        assertThat(dataSetVersionNumbers(dataSetDto.id().get())).containsExactlyElementsOf(versions);
    }

    @Then("the search for the dataset bring the last version")
    public void the_search_for_the_dataset_bring_the_last_version() {
        DataSetDto dataSetDto = lastSavedDataSetDto();
        findDataSet(dataSetDto.id().get(), null);
        assertThat(foundDataSetDto).isEqualTo(dataSetDto);
    }

    @Then("the dataset version (.*) can be found")
    public void the_dataset_version_can_be_found(Integer version) {
        DataSetDto dataSetDto = lastSavedDataSetDto();
        findDataSet(dataSetDto.id().get(), version);
        assertThat(foundDataSetDto).isEqualTo(savedDataSetDtos.get(version - 1));
    }

    private void saveDataSet(DataSetDto dataSetDto) {
        try {
            final ResponseEntity<String> responseEntity = restClient.defaultRequest()
                .withUrl(DataSetController.BASE_URL)
                .withBody(dataSetDto)
                .post(String.class);

            LOGGER.info("New dataset saved : " + dataSetDto);
            String body = requireNonNull(responseEntity.getBody());
            savedDataSetDtos.add(ImmutableDataSetDto.builder().from(dataSetDto).id(body).build());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IllegalArgumentException("Unable to save dataset [" + dataSetDto + "]");
        }
    }

    private void findDataSet(String dataSetId, Integer version) {
        try {
            final ResponseEntity<String> responseEntity = restClient.defaultRequest()
                .withUrl(DataSetController.BASE_URL + "/" + dataSetId + Optional.ofNullable(version).map(s -> "/" + s).orElse(""))
                .get();

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            foundDataSetDto = om.readValue(responseEntity.getBody(), DataSetDto.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IllegalArgumentException("Unable to find dataset [" + dataSetId + "] with version [" + version + "]");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read found dataset [" + dataSetId + "] with version [" + version + "]");
        }
    }

    private void deleteDataSet(DataSetDto dataSetDto) {
        try {
            restClient.defaultRequest()
                .withUrl(DataSetController.BASE_URL + "/" + dataSetDto.id().get())
                .delete();

            LOGGER.info("Dataset deleted : " + dataSetDto);
            assertThat(savedDataSetDtos.remove(dataSetDto)).isTrue();
            deletedDataSetDtos.add(dataSetDto);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IllegalArgumentException("Unable to delete dataset [" + dataSetDto.id().get() + "]");
        }
    }

    private void noFindDataSet(String dataSetId/*, Integer version*/) {
        try {
            restClient.defaultRequest()
                .withUrl(DataSetController.BASE_URL + "/" + dataSetId/* + Optional.ofNullable(version).map(s -> "/" + s).orElse("")*/)
                .get();
        } catch (HttpClientErrorException ce) {
            assertThat(ce.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } catch (HttpServerErrorException e) {
            throw new IllegalArgumentException("Unable to not find dataset [" + dataSetId/*+ "] with version [" + version + "]"*/);
        }
    }

    private Integer dataSetLastVersionNumber(String dataSetId) {
        try {
            final ResponseEntity<String> responseEntity = restClient.defaultRequest()
                .withUrl(DataSetController.BASE_URL + "/" + dataSetId + "/versions/last")
                .get();

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            String body = requireNonNull(responseEntity.getBody());
            return Integer.valueOf(body);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IllegalArgumentException("Unable to find dataset [" + dataSetId + "] last version number");
        }
    }

    private List<Integer> dataSetVersionNumbers(String dataSetId) {
        try {
            final ResponseEntity<String> responseEntity = restClient.defaultRequest()
                .withUrl(DataSetController.BASE_URL + "/" + dataSetId + "/versions")
                .get();

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            return om.readValue(responseEntity.getBody(), new TypeReference<List<Integer>>() {
            });
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IllegalArgumentException("Unable to find dataset [" + dataSetId + "] version numbers");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read found dataset [" + dataSetId + "] version numbers");
        }
    }

    private DataSetDto lastSavedDataSetDto() {
        return savedDataSetDtos.get(savedDataSetDtos.size() - 1);
    }
}
