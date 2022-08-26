package com.chutneytesting.admin.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InfoControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() throws Exception {
        InfoController infoController = new InfoController();

        Field pinCodeField = infoController.getClass().getDeclaredField("applicationName");
        pinCodeField.setAccessible(true);
        pinCodeField.set(infoController, "my application description");
        mockMvc = MockMvcBuilders.standaloneSetup(infoController)
            .build();
    }

    @Test
    public void should_return_chutney_version() throws Exception {
        this.mockMvc.perform(get("/api/v1/info/version")).andDo(print()).andExpect(status().isOk())
            .andExpect(content().string(matchesRegex("\\d\\.\\d\\.\\d+(-SNAPSHOT)?")));
    }

    @Test
    public void should_return_application_name() throws Exception {
        this.mockMvc.perform(get("/api/v1/info/appname")).andDo(print()).andExpect(status().isOk())
            .andExpect(content().string(containsString("my application description")));
    }
}
