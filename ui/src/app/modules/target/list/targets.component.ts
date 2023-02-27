import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, Subscriber } from 'rxjs';
import { FileSaverService } from 'ngx-filesaver';

import { Environment, Target } from '@model';
import { ValidationService } from '../../../molecules/validation/validation.service';
import { EnvironmentService } from '@core/services/environment.service';
import { distinct, filterOnTextContent, match } from '@shared/tools';

@Component({
    selector: 'chutney-targets',
    templateUrl: './targets.component.html',
    styleUrls: ['./targets.component.scss']
})
export class TargetsComponent implements OnInit {

    errorMessage: string = null;
    environments: Environment[] = [];
    targetsNames: string[] = [];
    targets: Target[] = [];

    environmentFilter: Environment;
    targetFilter = '';


    // TODO remove ?
    envForm: FormGroup;
    envUpdate = false;
    help: boolean;

    constructor(
        private environmentService: EnvironmentService,
        public validationService: ValidationService,
        private fileSaverService: FileSaverService,
        private formBuilder: FormBuilder) {
    }

    ngOnInit() {
        this.loadTargets();
    }

    private loadTargets() {
        this.environmentService.list().subscribe({
            next: envs => {
                this.environments = envs;
                this.targets = envs.flatMap(env => env.targets);
                this.targetsNames = distinct(this.targets.map(target => target.name));
            },
            error: error => this.errorMessage = error.error
        });
    }


    findTarget(targetName: string, environment: Environment): Target {
        return environment?.targets.find(target => target.name === targetName);
    }

    deleteTarget(name: string) {
        /*this.environmentService.deleteTarget(this.selectedEnvironment.name, target.name).subscribe(
            (res) => {
                this.loadTarget();
            },
            (error) => {
                console.log(error);
                this.errorMessage = error.error;
            }
        );*/
    }

    updateSelectedTarget() {
        //this.updateTarget(this.selectedTargetName, this.selectedTarget);
    }

    updateTarget(oldTargetName: string, newTarget: Target) {
        /* if (!this.isValid(newTarget)) {
             this.errorMessage = 'Name cannot be empty and url must match xxx://xxxxx:12345 or a spel (${#dynamicUri})';
         } else {

             this.environmentService.updateTarget(this.selectedEnvironmentName.name, oldTargetName, newTarget).subscribe(
                 (res) => {
                     this.selectedTargetName = newTarget.name;
                     this.loadTarget();
                 },
                 (error) => {
                     console.log(error);
                     this.errorMessage = error.toString();
                 }
             );

         }*/
    }

    addTarget(target: Target) {
        /*this.environmentService.addTarget(this.selectedEnvironmentName.name, target).subscribe(
            (savedTarget) => {
                this.loadTarget();
            },
            (error) => {
                console.log(error);
                this.errorMessage = error.error;
            }
        );*/
    }

    exportTarget() {
        /*const fileName = `${this.selectedEnvironmentName.name}-${this.selectedTarget.name}.chutney.json`;
        this.environmentService.exportTarget(this.selectedEnvironmentName.name, this.selectedTarget.name).subscribe(
            res => {
                this.fileSaverService.saveText(JSON.stringify(res), fileName);
            },
            error => {
                console.log(error);
                this.errorMessage = error.error;
            }
        );*/
    }

    importTarget(files: Array<File>) {
        files.map(f => this.toTarget(f).subscribe(
            (t) => {
                if (!this.isValid(t)) {
                    this.errorMessage +=
                        '<br>Error found in ' + f.name + ', target name cannot be empty and url must match xxx://xxxxx:12345 or a spel (${#dynamicUri})';
                } else {
                    try {
                        const duplicates = this.findDuplicate(t);
                        if (duplicates.length !== 0) {
                            if (confirm('Target [' + t.name + '] exists already.\n\n Do you want to update it ?')) {
                                this.updateTarget(duplicates[0].name, t);
                            }
                        } else {
                            this.addTarget(t);
                            console.log('Upload ' + t.name + ': ' + t.url);
                        }
                    } catch (error) {
                        console.error('File upload failed.');
                        console.error(error);
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
        //return this.targets.filter(t => Object.is(t.name, target.name));
        return [];
    }

    cancelSelectedTarget() {
        /*this.selectedTarget = null;
        this.errorMessage = null;*/
    }

    isValid(target: Target): boolean {
        return this.validationService.isNotEmpty(target.name)
            && this.validationService.isValidUrlOrSpel(target.url);
    }

    deleteEnvironment() {
        /*this.environmentService.delete(this.activeEnvironment).subscribe(
            (res) => {
                this.reload();
            },
            (error) => {
                this.errorMessage = error.error;
            }
        );*/
    }

    initEnvironment() {
        this.cancelSelectedTarget();
        this.envForm = this.formBuilder.group({
            name: ['', Validators.required],
            description: ''
        });
    }

    initForUpdateEnvironment() {
        /*this.cancelSelectedTarget();
        this.envForm = this.formBuilder.group({
            name: [this.selectedEnvironmentName.name, Validators.required],
            description: this.selectedEnvironmentName.description
        });
        this.envUpdate = true;*/
    }

    updateEnvironment() {
        /*const name = this.envForm.value['name'];
        const description = this.envForm.value['description'];
        if (!this.envUpdate) {
            this.environmentService.create(new Environment(name, description)).subscribe(
                (res) => this.reload(),
                (error) => {
                    console.log(error);
                    this.errorMessage = error.error;
                }
            );
        } else {
            this.environmentService.update(this.activeEnvironment, new Environment(name, description))
                .subscribe(
                    (res) => this.reload(),
                    (error) => {
                        console.log(error);
                        this.errorMessage = error.error;
                    }
                );
        }*/
    }

    // Import/Export Env------------------------------------------------

    exportEnvironment() {
        /*const fileName = `env.${this.activeEnvironment}.chutney.json`;
        this.environmentService.export(this.activeEnvironment).subscribe(
            res => {
                this.fileSaverService.saveText(JSON.stringify(res), fileName);
            },
            error => {
                console.log(error);
                this.errorMessage = error.error;
            }
        );*/
    }

    private isValidEnv(env: Environment): boolean {
        return this.validationService.isNotEmpty(env.name);
    }

    private nameAlreadyExistFor(env: Environment) {
        const duplicates = this.findDuplicateEnv(env);
        return duplicates.length !== 0;
    }

    private findDuplicateEnv(env: Environment): Environment[] {
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
                            if (confirm('Environment [' + env.name + '] exists already.\n\n Do you want to update it ?')) {
                                this.environmentService.update(env.name, env).subscribe(
                                    (res) => {
                                        this.errorMessage = env.name + ' has been updated';
                                    },
                                    (error) => {
                                        this.errorMessage = error.error;
                                    }
                                );
                            }
                        } else {
                            this.environmentService.create(env).subscribe(
                                (res) => {
                                    this.environments.push(env);
                                    this.environments.sort((t1, t2) => t1.name.toUpperCase() > t2.name.toUpperCase() ? 1 : 0);
                                    this.errorMessage = env.name + ' has been created';
                                },
                                (error) => {
                                    this.errorMessage = error.error;
                                }
                            );
                        }
                    } catch (error) {
                        console.error('File upload failed.');
                        console.error(error);
                        this.errorMessage += '<br>' + error.toString();
                    }
                }
            }
        );
    }

    private toEnvironment(file: File): Observable<Environment> {
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
            this.errorMessage = null;
            this.envUpdate = null;
            this.envForm = null;
            this.loadTargets();
        })();

    }

    delay(ms: number) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }



    exist(targetName: string, environment: Environment): boolean {
        return !!this.findTarget(targetName, environment);
    }



    activeEnvironmentTab(targetName: string) : string{
        if (this.environmentFilter) {
            return this.environmentFilter.name;
        }
        return this.environments.find(env =>
            this.targetFilter ? this.matchEnv(env, targetName): this.exist(targetName, env)
        ).name;
    }

    private matchEnv(env: Environment, targetName) {
        return !!env.targets.find(target => target.name === targetName && this.match(target));
    }

    private filterByKeyword(targets: Target[]): Target[] {
        if (!!this.targetFilter) {
            return  targets.filter(target => this.match(target));
        }
        return targets;

    }

    filter(env: Environment = null) {
        if (env) {
            if (env.name === this.environmentFilter?.name) {
                this.environmentFilter = null;
                this.targets = this.environments.flatMap(e => e.targets);
            } else {
                this.environmentFilter = env;
                this.targets = env.targets;
            }
        }

       this.targetsNames = distinct(this.filterByKeyword(this.targets).map(target => target.name));
    }

    private match(target: Target): boolean{
        return match(target.name, this.targetFilter) || match(target.url, this.targetFilter) ||
            filterOnTextContent(target.properties, this.targetFilter, ['key', 'value'])?.length;
    }
}
