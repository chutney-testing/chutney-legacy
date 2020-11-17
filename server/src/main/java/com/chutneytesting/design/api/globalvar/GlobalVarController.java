package com.chutneytesting.design.api.globalvar;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.infra.storage.globalvar.FileGlobalVarRepository;
import java.util.Set;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ui/globalvar/v1")
public class GlobalVarController {

    private final GlobalvarRepository globalVarRepository;

    public GlobalVarController(FileGlobalVarRepository globalVarRepository) {
        this.globalVarRepository = globalVarRepository;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Set<String> list() {
        return globalVarRepository.list();
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/{fileName}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void save(@PathVariable("fileName") String fileName, @RequestBody TextDto textContent) {
        try {
            globalVarRepository.saveFile(fileName, JsonValue.readHjson(textContent.getMessage()).toString(Stringify.HJSON));
        } catch (Exception e) {
            throw new RuntimeException("Not valid hjson", e);
        }
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping(path = "/{fileName}")
    public void delete(@PathVariable("fileName") String fileName) {
        globalVarRepository.deleteFile(fileName);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/{fileName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public TextDto getFile(@PathVariable("fileName") String fileName) {
        return new TextDto(globalVarRepository.getFile(fileName));
    }

    public static class TextDto {
        private String message;

        public TextDto(@JsonProperty("message") String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

}
