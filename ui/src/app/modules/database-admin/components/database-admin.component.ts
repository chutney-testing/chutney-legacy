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

import { Component } from '@angular/core';

import { Execution } from '@model';
import { DatabaseAdminService } from '@core/services';
import { ActivatedRoute, Params, Router } from '@angular/router';

@Component({
    selector: 'chutney-database-admin',
    templateUrl: './database-admin.component.html',
    styleUrls: ['./database-admin.component.scss']
})
export class DatabaseAdminComponent {

    query: string;
    errorMessage: string;
    executions: Execution[];
    private _executionsFilters: Params = {};


    constructor(
        private databaseAdminService: DatabaseAdminService,
        private route: ActivatedRoute,
        private router: Router) {
        this.executions = []
    }

    get executionsFilters(): Params {
        return this._executionsFilters;
    }

    set executionsFilters(value: Params) {
        const {open, active, ...executionsParams} = value;
        this._executionsFilters = executionsParams;
        this.updateQueryParams();
    }


    private updateQueryParams() {
        let queryParams = this.cleanParams({...this.executionsFilters});
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: queryParams
        });
    }

    openReport(request: { execution: Execution }) {
        const url = this.router.serializeUrl(this.router.createUrlTree(['scenario', request.execution.scenarioId, 'executions'], {queryParams: {open: request.execution.executionId, active: request.execution.executionId}}));
        console.log(url)
        window.open('#' + url, "_blank");
    }

    private cleanParams(params: Params) {
        Object.keys(params).forEach(key => {
            if (params[key] === null || params[key] === '' || params[key] === '0') {
                delete params[key];
            }
        });
        return params;
    }

    searchQuery() {
        if (this.query.length === 0) {
            return;
        }
        this.errorMessage = null;
        this.databaseAdminService.getExecutionReportMatchQuery(this.query)
        .subscribe(
            (res: Execution[]) => {
                this.executions = res;
            },
            (error) => {
                this.errorMessage = error
            }
        );
    }

    updateQuery(text: string) {
        this.query = text;
    }
}
