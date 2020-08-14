package com.chutneytesting.plugin.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/ui/plugins/v1")
public class PluginManagerController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getTestCase(
        @RequestParam("page") String page,
        @RequestParam("section") String section
    ) {
        return "{bou}";
    }

}

