package com.chutneytesting.admin.api;

import com.chutneytesting.admin.domain.HomePage;
import com.chutneytesting.admin.domain.HomePageRepository;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/homepage/v1")
@CrossOrigin(origins = "*")
public class HomePageController {
    private final HomePageRepository repository;

    public HomePageController(HomePageRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public HomePage load() {
        return repository.load();
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public HomePage save(@RequestBody HomePage homePage) {
        return repository.save(homePage);
    }

}
