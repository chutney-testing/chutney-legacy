import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, Subscriber } from 'rxjs';
import { FileSaverService } from 'ngx-filesaver';

import { EnvironmentMetadata, Target } from '@model';
import { ValidationService } from '../../molecules/validation/validation.service';
import { EnvironmentAdminService } from '@core/services';

@Component({
    selector: 'chutney-environment-admin',
    templateUrl: './environment-admin.component.html',
    styleUrls: ['./environment-admin.component.scss']
})
export class EnvironmentAdminComponent implements OnInit {

    errorMessage: string = null;
    targetFilter = '';

    selectedTargetName: string = null;
    selectedTarget: Target = null;
    targets: Array<Target> = [];

    environments: EnvironmentMetadata[];
    environmentsNames: Array<string>;
    selectedEnvironment: EnvironmentMetadata;
    envForm: FormGroup;
    envUpdate = false;

    constructor(
        private environmentAdminService: EnvironmentAdminService,
        public validationService: ValidationService,
        private fileSaverService: FileSaverService,
        private formBuilder: FormBuilder) {
    }

    ngOnInit() {
        this.loadEnvironment();
    }

    loadEnvironment() {
        this.environmentAdminService.listEnvironments().subscribe(
            (res) => {
                this.environments = res;
                this.selectedEnvironment = this.environments[0];
                this.environmentsNames = res.map(e => e.name);
                this.loadTarget();
            },
            (error) => {this.errorMessage = error.error; }
        );
    }

    loadTarget() {
        this.environmentAdminService.listTargets(this.selectedEnvironment.name).subscribe(
            (res) => {
                this.targets = res.sort((t1, t2) =>  t1.name.toUpperCase() > t2.name.toUpperCase() ? 1 : 0);
            },
            (error) => { console.log(error); this.errorMessage = error.error; }
        );
        this.selectedTarget = null;
        this.selectedTargetName = null;
    }

    selectTarget(target: Target) {
        this.selectedTarget = target;
        this.selectedTargetName = target.name;
    }

    deleteTarget(target: Target) {
        this.environmentAdminService.deleteTarget(this.selectedEnvironment.name, target.name).subscribe(
            (res) => {
                this.loadTarget();
            },
            (error) => { console.log(error); this.errorMessage = error.error; }
        );
    }

    updateSelectedTarget() {
        this.updateTarget(this.selectedTargetName, this.selectedTarget);
    }

    updateTarget(oldTargetName: string, newTarget: Target) {
        if (!this.isValid(newTarget)) {
            this.errorMessage = 'Name cannot be empty and url must match xxx://xxxxx:12345';
        } else {

            this.environmentAdminService.updateTarget(this.selectedEnvironment.name, oldTargetName, newTarget).subscribe(
                (res) => {
                    this.selectedTargetName = newTarget.name;
                    this.loadTarget();
                },
                (error) => { console.log(error); this.errorMessage = error.toString(); }
            );

        }
    }

    addTarget(target: Target) {
        this.environmentAdminService.addTarget(this.selectedEnvironment.name, target).subscribe(
            (savedTarget) => { this.loadTarget(); },
            (error) => { console.log(error); this.errorMessage = error.error; }
        );
    }

    exportTarget() {
        const fileName = `${this.selectedEnvironment.name}-${this.selectedTarget.name}.chutney.json`;
        this.environmentAdminService.exportTarget(this.selectedEnvironment.name, this.selectedTarget.name).subscribe(
            res => { this.fileSaverService.saveText(JSON.stringify(res), fileName); },
            error => { console.log(error); this.errorMessage = error.error; }
        );
    }

    importTarget(files: Array<File>) {
        files.map(f => this.toTarget(f).subscribe(
            (t) => {
                if (!this.isValid(t)) {
                    this.errorMessage +=
                        '<br>Error found in ' + f.name + ', target name cannot be empty and url must match xxx://xxxxx:12345';
                } else {
                    try {
                        const duplicates = this.findDuplicate(t);
                        if (duplicates.length !== 0) {
                            if (confirm('Target ['  + t.name + '] exists already.\n\n Do you want to update it ?')) {
                                this.updateTarget(duplicates[0].name, t);
                            }
                        } else {
                            this.addTarget(t);
                            console.log('Upload '  + t.name + ': ' + t.url);
                        }
                    } catch ( error ) {
                        console.error( 'File upload failed.' );
                        console.error( error );
                        this.errorMessage += '<br>' + error.toString();
                    }
                }
            }
        ));
    }

    private toTarget(file: File): Observable<Target> {
        return Observable.create(
            (sub: Subscriber<string>): void => {
                const r = new FileReader();
                r.onload = (ev: ProgressEvent): void => {
                    let target;
                    try {
                        target = JSON.parse((ev.target as any).result);
                    } catch (ex) {
                        this.errorMessage += '<br>' + 'Error found in: ' + file.name + ' -> ' + ex.toString();
                    }
                    sub.next(target);
                };
                r.readAsText(file);
            }
        );
    }

    private findDuplicate(target: Target): Target[] {
        return this.targets.filter(t => Object.is(t.name, target.name));
    }

    cancel() {
        this.selectedTarget = null;
        this.selectedTargetName = null;
    }

    scrollToTop() {
        window.scroll(0, 0);
    }

    isValid(target: Target): boolean {
        return this.validationService.isNotEmpty(target.name)
            && this.validationService.isValidUrl(target.url);
    }

    initEnvironment() {
        this.envForm = this.formBuilder.group({
            name: ['', Validators.required],
            description: ''
        });
    }

    initForUpdateEnvironment() {
        this.envForm = this.formBuilder.group({
            name: [this.selectedEnvironment.name, Validators.required],
            description: this.selectedEnvironment.description
        });
        this.envUpdate = true;
    }

    updateEnvironment () {
        const name = this.envForm.value['name'];
        const description = this.envForm.value['description'];
        if (!this.envUpdate) {
            this.environmentAdminService.createEnvironment(new EnvironmentMetadata(name, description)).subscribe(
                (res) => this.reload(),
                (error) => { console.log(error); this.errorMessage = error.error; }
            );
        } else {
            this.environmentAdminService.updateEnvironment(this.selectedEnvironment.name, new EnvironmentMetadata(name, description))
            .subscribe(
                (res) => this.reload(),
                (error) => { console.log(error); this.errorMessage = error.error; }
            );
        }
    }

    // Import/Export Env------------------------------------------------

	exportEnvironment() {
        const fileName = `env.${this.selectedEnvironment.name}.chutney.json`;
        this.environmentAdminService.exportEnvironment(this.selectedEnvironment.name).subscribe(
            res => { this.fileSaverService.saveText(JSON.stringify(res), fileName); },
            error => { console.log(error); this.errorMessage = error.error; }
        );
    }

    private isValidEnv(env: EnvironmentMetadata): boolean {
        return this.validationService.isNotEmpty(env.name);
    }

    private nameAlreadyExistFor(env: EnvironmentMetadata) {
        const duplicates = this.findDuplicateEnv(env);
        return duplicates.length !== 0;
    }

    private findDuplicateEnv(env: EnvironmentMetadata): EnvironmentMetadata[] {
        return this.environments.filter(e => Object.is(e.name, env.name));
    }

    importEnvironment(file: File) {
        this.toEnvironment(file).subscribe(
            (env) => {
                if (!this.isValidEnv(env)) {
                    this.errorMessage +=
                        '<br>Error found in ' + file.name + ', environment name cannot be empty and url must match xxx://xxxxx:12345';
                } else {
                    try {
                        if (this.nameAlreadyExistFor(env)) {
                            if (confirm('Environment ['  + env.name + '] exists already.\n\n Do you want to update it ?')) {
                                this.environmentAdminService.updateEnvironment(env.name, env).subscribe(
                                    (res) => { this.errorMessage = env.name + ' has been updated'; },
                                    (error) => { this.errorMessage = error.error;}
                                );
                            }
                        } else {
                            this.environmentAdminService.createEnvironment(env).subscribe(
                                (res) => {
                                    this.environments.push(env);
                                    this.environments.sort((t1, t2) =>  t1.name.toUpperCase() > t2.name.toUpperCase() ? 1 : 0);
                                    this.errorMessage = env.name + ' has been created';
                                },
                                (error) => { this.errorMessage = error.error;}
                            );
                        }
                    } catch ( error ) {
                        console.error( 'File upload failed.' );
                        console.error( error );
                        this.errorMessage += '<br>' + error.toString();
                    }
                }
            }
        );
    }

    private toEnvironment(file: File): Observable<EnvironmentMetadata> {
        return Observable.create(
            (sub: Subscriber<string>): void => {
                const r = new FileReader();
                r.onload = (ev: Event): void => {
                    let environment;
                    try {
                        environment = JSON.parse((ev.target as any).result);
                    } catch (ex) {
                        this.errorMessage += '<br>' + 'Error found in: ' + file.name + ' -> ' + ex.toString();
                    }
                    sub.next(environment);
                };
                r.readAsText(file);
            }
        );
    }

    // Import/Export Env------------------------------------------------

    reload() {
        (async () => {
            await this.delay(500);
            this.envUpdate = null;
            this.envForm = null;
            this.loadEnvironment();
          })();

    }

    delay(ms: number) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    cancelEnvironment() {
        this.envForm = null;
    }

    changingValue(envName: string) {
        this.selectedEnvironment = this.environments.filter(e => e.name === envName)[0];
        this.loadTarget();
    }
}
