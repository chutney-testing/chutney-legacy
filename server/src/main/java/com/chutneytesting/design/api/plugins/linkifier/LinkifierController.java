package com.chutneytesting.design.api.plugins.linkifier;

import com.chutneytesting.design.domain.plugins.linkifier.Linkifier;
import com.chutneytesting.design.domain.plugins.linkifier.Linkifiers;
import java.util.List;
import java.util.stream.Collectors;
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
@RequestMapping("/api/v1/ui/plugins/linkifier/")
@CrossOrigin(origins = "*")
public class LinkifierController {

    private final Linkifiers linkifiers;

    public LinkifierController(Linkifiers linkifiers) {
        this.linkifiers = linkifiers;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<LinkifierDto> getAllLinkifier() {
        return linkifiers.getAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public LinkifierDto saveLinkifier(@RequestBody LinkifierDto linkifierDto) {
        return toDto(linkifiers.add(new Linkifier(linkifierDto.pattern(), linkifierDto.link(), linkifierDto.id())));
    }

    @DeleteMapping(path = "/{id}")
    public void removeLinkifier(@PathVariable("id") String id) {
        linkifiers.remove(id);
    }

    private LinkifierDto toDto(Linkifier linkifier) {
        return ImmutableLinkifierDto.builder()
            .pattern(linkifier.pattern)
            .link(linkifier.link)
            .id(linkifier.id)
            .build();
    }
}
