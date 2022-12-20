package com.chutneytesting.admin.api;

import com.chutneytesting.admin.domain.Backup;
import com.chutneytesting.admin.domain.BackupRepository;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
@RequestMapping("/api/v1/backups")
@CrossOrigin(origins = "*")
public class BackupController {

    private final BackupRepository backupRepository;

    public BackupController(BackupRepository backupRepository) {
        this.backupRepository = backupRepository;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String backup(@RequestBody Backup backup) {
        return backupRepository.save(backup);
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @DeleteMapping(path = "/{backupId}")
    public void delete(@PathVariable("backupId") String backupId) {
        backupRepository.delete(backupId);
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = "/{backupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Backup get(@PathVariable("backupId") String backupId) {
        return backupRepository.read(backupId);
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = "/{backupId}/download", produces = "application/zip")
    public void getBackupData(HttpServletResponse response, @PathVariable("backupId") String backupId) throws IOException {
        backupRepository.getBackupData(backupId, response.getOutputStream());
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Backup> list() {
        return backupRepository.list();
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = "/backupables", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getBackupables() {
        return backupRepository.getBackupables();
    }
}
