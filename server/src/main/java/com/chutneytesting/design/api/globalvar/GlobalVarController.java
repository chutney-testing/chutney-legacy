package com.chutneytesting.design.api.globalvar;

import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.infra.storage.globalvar.FileGlobalVarRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/ui/globalvar/v1")
public class GlobalVarController {

    private final GlobalvarRepository globalVarRepository;

    public GlobalVarController(FileGlobalVarRepository globalVarRepository) {
        this.globalVarRepository = globalVarRepository;
    }

    @PreAuthorize("hasAuthority('GLOBAL_VAR_READ')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> list() {
        return globalVarRepository.list();
    }

    @PreAuthorize("hasAuthority('GLOBAL_VAR_WRITE')")
    @PostMapping(path = "/{fileName}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void save(@PathVariable("fileName") String fileName, @RequestBody TextDto textContent) {
        try {
            validateYaml(textContent);
            globalVarRepository.saveFile(fileName, textContent.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Not valid yaml: " + e.getMessage(), e);
        }
    }

    private void validateYaml(TextDto textContent) {
        Yaml yaml = new Yaml();
        yaml.load(textContent.getMessage());
    }

    @PreAuthorize("hasAuthority('GLOBAL_VAR_WRITE')")
    @DeleteMapping(path = "/{fileName}")
    public void delete(@PathVariable("fileName") String fileName) {
        globalVarRepository.deleteFile(fileName);
    }

    @PreAuthorize("hasAuthority('GLOBAL_VAR_READ')")
    @GetMapping(path = "/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TextDto getFile(@PathVariable("fileName") String fileName) {
        return new TextDto(globalVarRepository.getFileContent(fileName));
    }

    public static class TextDto {
        private final String message;

        public TextDto(@JsonProperty("message") String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

}
