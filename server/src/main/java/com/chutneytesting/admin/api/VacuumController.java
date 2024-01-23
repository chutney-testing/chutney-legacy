package com.chutneytesting.admin.api;

import com.chutneytesting.admin.domain.DBVacuum;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/database")
@CrossOrigin(origins = "*")
public class VacuumController {

    private final DBVacuum dbVacuum;

    public VacuumController(DBVacuum dbVacuum) {
        this.dbVacuum = dbVacuum;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping(path = "/compact")
    public void vacuum() {
        dbVacuum.vacuum();
    }
}
