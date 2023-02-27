import { Component, OnInit } from '@angular/core';
import { Environment } from '@model';
import { ActivatedRoute } from '@angular/router';
import { EnvironmentService } from '@core/services';
import { ValidationService } from '../../../molecules/validation/validation.service';

@Component({
    selector: 'chutney-environments',
    templateUrl: './environments.component.html',
    styleUrls: ['./environments.component.scss']
})
export class EnvironmentsComponent implements OnInit {

    editableEnvironments: Environment[] = [];
    environments: Environment[] = [];
    environment: Environment;
    editionIndex: number;
    errorMessage: string;

    constructor(private route: ActivatedRoute,
                private environmentService: EnvironmentService,
                private validationService: ValidationService) {
    }

    ngOnInit(): void {
        this.route.data.subscribe((data: { environments: Environment[] }) => {
            this.editableEnvironments = data.environments;
            this.environments = data.environments.map(env => ({...env}));
        });
    }

    editing(index: number): boolean {
        return this.editionIndex === index;
    }

    save(index: number) {
        this.environmentService.update(this.environments[index].name, this.editableEnvironments[index]).subscribe({
            next: () => {
                this.environments[index] = {...this.editableEnvironments[index]};
                this.sort();
                this.editionIndex = null;
            },
            error: err => this.errorMessage = err.error
        });
    }

    enableAdd() {
        this.environment = new Environment('', '');
    }

    add() {
        this.environmentService.create(this.environment).subscribe({
            next: () => {
                this.editableEnvironments.push(this.environment);
                this.environments.push(this.environment);
                this.sort();
                this.environment = null;
            },
            error: err => this.errorMessage = err.error
        });
    }

    delete(name: string, index: number) {
        this.environmentService.delete(name).subscribe({
            next: () => {
                this.editableEnvironments.splice(index, 1);
                this.environments.splice(index, 1);
            },
            error: err => this.errorMessage = err.error
        })
    }

    export(env: Environment) {
        this.environmentService.export(env.name).subscribe({
            error: err => this.errorMessage = err.error
        });
    }

    import(file: File) {
        this.environmentService.import(file).subscribe({
            next: env => {
                this.editableEnvironments.push(env);
                this.environments.push(env);
                this.sort();
            },
            error: err => this.errorMessage = err.error
        })
    }

    private sort() {
        this.environments.sort((e1, e2) => e1.name.toUpperCase() > e2.name.toUpperCase() ? 1 : -1)
        this.editableEnvironments.sort((e1, e2) => e1.name.toUpperCase() >= e2.name.toUpperCase() ? 1 : -1)
    }
}
