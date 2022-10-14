import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { BackupsService, BackupDto } from '@core/services/backups.service';
import { Backup } from '@core/model/backups.model';
import { timer } from 'rxjs';
import { FileSaverService } from 'ngx-filesaver';

@Component({
    selector: 'chutney-backups-admin',
    templateUrl: './backups-admin.component.html',
    styleUrls: ['./backups-admin.component.scss']
})
export class BackupsAdminComponent implements OnInit {

    backups: Array<Backup> = [];
    backupForm: FormGroup;

    constructor(
        private backupsService: BackupsService,
        private formBuilder: FormBuilder,
        private fileSaverService: FileSaverService
    ) {
        this.initBackupForm();
    }

    ngOnInit(): void {
        this.loadBackups();
    }

    launchBackup() {
        this.backupsService.save(
            new BackupDto(
                this.formValue('agentsNetwork'),
                this.formValue('environments'),
                this.formValue('components'),
                this.formValue('globalVars'),
                this.formValue('jiraLinks')
            )
        ).subscribe(() => this.reloadAfter(0));
    }

    deleteBackup(backup: Backup) {
        this.backupsService.delete(backup).subscribe(() => this.reloadAfter(100));
    }

    download(backup: Backup) {
        this.backupsService.download(backup).subscribe(res => {
            const blob = new Blob([res], { type: 'application/zip' });
            this.fileSaverService.save(blob, backup.id() + '.zip');
          });
    }

    isOneBackupSelected(): boolean {
        return this.formValue('agentsNetwork') ||
        this.formValue('environments') ||
        this.formValue('components') ||
        this.formValue('globalVars') ||
        this.formValue('jiraLinks');
    }

    private loadBackups() {
        this.backupsService.list()
            .subscribe(res => this.backups = res);
    }

    private initBackupForm() {
        this.backupForm = this.formBuilder.group({
            agentsNetwork: true,
            environments: true,
            components: true,
            globalVars: true,
            jiraLinks: true,
        });
    }

    private formValue(name: string): boolean {
        return this.backupForm.get(name).value;
    }

    private reloadAfter(time: number) {
        if (time > 0) {
            timer(time).subscribe(() =>
                this.loadBackups()
            );
        } else {
            this.loadBackups();
        }
    }

}
