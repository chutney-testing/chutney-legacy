package com.chutneytesting.admin.api;

import static com.chutneytesting.ServerConfiguration.SERVER_INSTANCE_NAME_VALUE;
import static com.chutneytesting.tools.loader.ExtensionLoaders.Sources.classpath;

import com.chutneytesting.tools.loader.ExtensionLoader;
import java.io.IOException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/info")
@CrossOrigin(origins = "*")
public class InfoController {

    public static final String BASE_URL = "/api/v1/info";

    @Value(SERVER_INSTANCE_NAME_VALUE)
    private String applicationName;

    @GetMapping("version")
    public String getVersion() throws IOException {
        return getChutneyVersion();
    }

    @GetMapping("appname")
    public String getApplicationName() throws IOException {
        return applicationName;
    }

    private String getChutneyVersion() {
        return ExtensionLoader.Builder
            .<String, String>withSource(classpath("META-INF/build.properties"))
            .withMapper(Collections::singleton)
            .load()
            .stream()
            .filter(text -> text.contains("chutneytesting"))
            .map(text -> getVersionInProperties(text))
            .findFirst().get();
    }

    private String getVersionInProperties(String text) {
        return text.lines()
            .filter(line -> line.startsWith("build.version="))
            .map(s -> s.split("=")[1])
            .findFirst().orElse("no version");
    }
}
