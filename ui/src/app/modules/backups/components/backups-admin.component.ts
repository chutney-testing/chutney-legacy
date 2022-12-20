import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, FormArray, AbstractControl } from '@angular/forms';

import { BackupsService } from '@core/services/backups.service';
import { Backup } from '@core/model/backups.model';
import { timer } from 'rxjs';
import { FileSaverService } from 'ngx-filesaver';

@Component({
    selector: 'chutney-backups-admin',
    templateUrl: './backups-admin.component.html',
    styleUrls: ['./backups-admin.component.scss']
})
export class BackupsAdminComponent implements OnInit {

    backups: Backup[] = [];
    backupForm: FormArray;
    backupables: string[];

    ngOnInit(): void {
        this.loadBackups();
    }

    constructor(
        private backupsService: BackupsService,
        private formBuilder: FormBuilder,
        private fileSaverService: FileSaverService
    ) {
        this.backupsService.getBackupables()
            .subscribe(backupables => {
                this.backupables = backupables;
                this.initBackupForm();
            })
    }

    launchBackup() {
        const backupFormValue = this.getBackupFormValue();
        const backup = new Backup(backupFormValue.filter(backupable => backupable.selected)
            .map(backupable => backupable.backupable));
        this.backupsService.save(backup)
            .subscribe(() => this.reloadAfter(0));
    }

    private getBackupFormValue(): { backupable: string, selected: boolean } [] {
        const backupFormValue: { backupable: string, selected: boolean } [] = this.backupForm.value;
        return backupFormValue;
    }

    deleteBackup(backup: Backup) {
        this.backupsService.delete(backup.id).subscribe(() => this.reloadAfter(100));
    }

    download(backup: Backup) {
        this.backupsService.download(backup.id).subscribe(res => {
            const blob = new Blob([res], {type: 'application/zip'});
            this.fileSaverService.save(blob, backup.id + '.zip');
        });
    }

    isOneBackupSelected(): boolean {
        const backupFormValue = this.getBackupFormValue();
        return !!backupFormValue.filter(backupable => backupable.selected).length;
    }

    private loadBackups() {
        this.backupsService.list()
            .subscribe(res => this.backups = res);
    }

    private initBackupForm() {
        this.backupForm = this.formBuilder.array(
            this.backupables.map(backupable => this.formBuilder.group({
                backupable: backupable,
                selected: true
            })));
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

    asFormGroup(formGroup: AbstractControl): FormGroup {
        return formGroup as FormGroup
    }
}
