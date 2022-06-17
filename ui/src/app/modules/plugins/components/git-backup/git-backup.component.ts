import { Component, OnInit } from '@angular/core';
import { ValidationService } from '../../../../molecules/validation/validation.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { delay } from '@shared/tools';
import { GitRemoteConfig } from '@model';
import { GitBackupService } from '@core/services';

@Component({
    selector: 'chutney-config-git-backup',
    templateUrl: './git-backup.component.html',
    styleUrls: ['./git-backup.component.scss']
})
export class GitBackupComponent implements OnInit {

    remoteConfigForm: FormGroup;

    message: string;
    isErrorNotification = false;

    remotes: Array<GitRemoteConfig> = [];

    constructor(private fb: FormBuilder,
                private gitBackupService: GitBackupService,
                private validationService: ValidationService) {
    }

    ngOnInit() {
        this.remoteConfigForm = this.fb.group({
            name: ['', Validators.required],
            url: ['', Validators.required],
            branch: [''],
            privateKeyPath: ['', Validators.required],
            passphrase: ['', Validators.required],
        });

        this.loadRemotes();
    }

    private loadRemotes() {
        this.gitBackupService.loadConfig().subscribe(
            (remotes: Array<GitRemoteConfig>) => {
                this.remotes = remotes;
                if (this.remotes.length > 0) {
                    this.remoteConfigForm.controls['name'].patchValue(remotes[0].name);
                    this.remoteConfigForm.controls['url'].patchValue(remotes[0].url);
                    this.remoteConfigForm.controls['branch'].patchValue(remotes[0].branch);
                    this.remoteConfigForm.controls['privateKeyPath'].patchValue(remotes[0].privateKeyPath);
                    this.remoteConfigForm.controls['passphrase'].patchValue(remotes[0].privateKeyPassphrase);
                }
            },
            (error) => {
                this.notify(error.error, true);
            }
        );
    }

    isValid(): boolean {
        return this.validationService.isNotEmpty(this.remoteConfigForm.value['name'])
            && this.validationService.isNotEmpty(this.remoteConfigForm.value['url'])
            && this.validationService.isNotEmpty(this.remoteConfigForm.value['privateKeyPath']);
    }

    saveConfig() {
        const remoteConfig = new GitRemoteConfig(
            this.remoteConfigForm.value['name'],
            this.remoteConfigForm.value['url'],
            this.remoteConfigForm.value['branch'],
            this.remoteConfigForm.value['privateKeyPath'],
            this.remoteConfigForm.value['passphrase']
        );
        this.gitBackupService.add(remoteConfig).subscribe(
            (res) => {
                this.notify('Remote configuration added', false);
                this.loadRemotes();
            },
            (error) => {
                this.notify(error.error, true);
            }
        );
    }

    edit(remote: GitRemoteConfig) {
        this.remoteConfigForm.controls['name'].patchValue(remote.name);
        this.remoteConfigForm.controls['url'].patchValue(remote.url);
        this.remoteConfigForm.controls['branch'].patchValue(remote.branch);
        this.remoteConfigForm.controls['privateKeyPath'].patchValue(remote.privateKeyPath);
        this.remoteConfigForm.controls['passphrase'].patchValue(remote.privateKeyPassphrase);
    }

    export(remote: GitRemoteConfig) {
        this.gitBackupService.backupTo(remote).subscribe(
            (res) => {
                this.notify('Chutney has been successfully backed up on ' + remote.name, false);
                this.loadRemotes();
            },
            (error) => {
                this.notify(error.error, true);
            }
        );
    }

    importFrom(remote: GitRemoteConfig) {
        if (confirm("/!\\ WARNING /!\\" +
            "\nImporting content will overwrite existing data." +
            "\nAre you sure you want to import content from " + remote.name + " ?")) {

            let name = prompt("Please enter the name of the repository you want to import:");
            if (name === remote.name) {
                this.gitBackupService.importFrom(remote).subscribe(
                    (res) => {
                        this.notify('Chutney has been successfully imported from ' + remote.name, false);
                        this.loadRemotes();
                    },
                    (error) => {
                        this.notify(error.error, true);
                    }
                );
            } else {
                this.notify('Unknown repository: ' + name, true);
            }
        }
    }

    remove(remote: GitRemoteConfig, i: number) {
        this.remotes.splice(i);
        this.gitBackupService.remove(remote).subscribe(
            (res) => {
                this.notify('Remote configuration removed', false);
                this.loadRemotes();
            },
            (error) => {
                this.notify(error.error, true);
            }
        );
    }

    notify(message: string, isErrorNotification: boolean) {
        (async () => {
            this.isErrorNotification = isErrorNotification;
            this.message = message;
            await delay(6000);
            this.message = null;
        })();
    }
}
