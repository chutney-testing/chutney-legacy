package com.chutneytesting.security.api;

import com.chutneytesting.security.domain.Authorizations;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authorizations")
@CrossOrigin(origins = "*")
public class AuthorizationController {

    private final Authorizations authorizations;

    public AuthorizationController(Authorizations authorizations) {
        this.authorizations = authorizations;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void save(@RequestBody AuthorizationsDto authorizations) {
        this.authorizations.save(AuthorizationMapper.fromDto(authorizations));
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AuthorizationsDto read() {
        return AuthorizationMapper.toDto(authorizations.read());
    }
}
