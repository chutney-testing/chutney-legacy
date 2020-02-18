package blackbox.stepdef;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import com.chutneytesting.task.api.TaskDto;
import blackbox.restclient.RestClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

public class TaskStepDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskStepDefs.class);

    private final RestClient secureRestClient;
    private final TestContext context;
    private ObjectMapper objectMapper;

    public TaskStepDefs(RestClient secureRestClient, TestContext context) {
        this.secureRestClient = secureRestClient;
        this.context = context;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @When("^request engine for all declared tasks$")
    public void request_all_tasks() {
        final ResponseEntity<String> response = secureRestClient.defaultRequest()
            .withUrl("/api/task/v1")
            .get();

        context.putExecutionReport(response.getBody());
    }

    @When("^request engine for task (.*)$")
    public void request_task(String taskId) {
        final ResponseEntity<String> response = secureRestClient.defaultRequest()
            .withUrl("/api/task/v1/"+taskId)
            .get();

        context.putExecutionReport(response.getBody());
    }

    @Then("^the following tasks are present in response$")
    public void check_tasks(DataTable expectedTasks) {
        Map<String, String> expectedTaskMap = expectedTasks.asMap(String.class, String.class);
        JSONArray jsonResponse = (JSONArray) JSONValue.parse((String)context.getExecutionReport());

        jsonResponse.forEach(jsonTask -> {
            String taskId = ((JSONObject)jsonTask).getAsString("identifier");
            String target = ((JSONObject)jsonTask).getAsString("target");
            if (!expectedTaskMap.containsKey(taskId)) {
                LOGGER.warn("Task {} not checked", taskId);
                // Assertions.fail("Task ["+taskId+"] not found in response");
            } else {
                TaskDto expectedTaskDto = new TaskDto(taskId, Boolean.valueOf(target), inputsFromDataTableRow(expectedTaskMap.get(taskId)));
                assertTaskAsJson(expectedTaskDto, (JSONObject) jsonTask);
            }
        });
    }

    @Then("^its (.*) are present in response$")
    public void check_task(String inputs) {
        JSONObject jsonResponse = (JSONObject) JSONValue.parse((String)context.getExecutionReport());

        TaskDto expectedTaskDto = new TaskDto(jsonResponse.getAsString("identifier"),
            Boolean.valueOf(jsonResponse.getAsString("target")),
            inputsFromDataTableRow(inputs));
        assertTaskAsJson(expectedTaskDto, jsonResponse);
    }

    private void assertTaskAsJson(TaskDto expectedTaskDto, JSONObject taskJson) {
        try {
            JSONAssert.assertEquals(objectMapper.writeValueAsString(expectedTaskDto), taskJson.toJSONString(), false);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<TaskDto.InputsDto> inputsFromDataTableRow(String taskDataString) {
        if (taskDataString.isEmpty()) {
            return Collections.emptyList();
        }

        String[] taskData = taskDataString.split(" ");
        List<TaskDto.InputsDto> inputs = new ArrayList<>();
        for (String s : taskData) {
            String[] ss = s.split(",");
            try {
                inputs.add(new TaskDto.InputsDto(ss[0], Class.forName(ss[1])));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return inputs;
    }
}
