/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';

import { Environment, Target } from '@model';
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
        private environmentService: EnvironmentService) {
    }

    ngOnInit() {
        this.loadTargets();
    }

    private loadTargets() {
        this.environmentService.list().subscribe({
            next: envs => {
                this.environments = envs;
                this.targets = envs.flatMap(env => env.targets).sort(this.targetSortFunction());
                this.targetsNames = distinct(this.targets.map(target => target.name));
            },
            error: error => this.errorMessage = error.error
        });
    }


    findTarget(targetName: string, environment: Environment): Target {
        return environment?.targets.find(target => target.name === targetName);
    }

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
            this.targets.sort(this.targetSortFunction());
        }

       this.targetsNames = distinct(this.filterByKeyword(this.targets).map(target => target.name));
    }

    private match(target: Target): boolean{
        return match(target.name, this.targetFilter) || match(target.url, this.targetFilter) ||
            filterOnTextContent(target.properties, this.targetFilter, ['key', 'value'])?.length;
    }

    private targetSortFunction(): (a: Target, b: Target) => number {
        return (t1, t2) => t1.name.localeCompare(t2.name);
    }
}
