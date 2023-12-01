/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.design.api.plugins.linkifier;

import com.chutneytesting.design.domain.plugins.linkifier.Linkifier;
import com.chutneytesting.design.domain.plugins.linkifier.Linkifiers;
import java.util.List;
import java.util.stream.Collectors;
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

@RestController
@RequestMapping("/api/v1/ui/plugins/linkifier/")
@CrossOrigin(origins = "*")
public class LinkifierController {

    private final Linkifiers linkifiers;

    public LinkifierController(Linkifiers linkifiers) {
        this.linkifiers = linkifiers;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LinkifierDto> getAllLinkifier() {
        return linkifiers.getAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LinkifierDto saveLinkifier(@RequestBody LinkifierDto linkifierDto) {
        return toDto(linkifiers.add(new Linkifier(linkifierDto.pattern(), linkifierDto.link(), linkifierDto.id())));
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
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
