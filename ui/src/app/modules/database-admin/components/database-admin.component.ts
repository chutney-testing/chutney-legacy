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

import { Sqlresult, sqlResultFromObject } from '@model';
import { PaginationInstance } from 'ngx-pagination';
import { DatabaseAdminService } from '@core/services';

@Component({
    selector: 'chutney-database-admin',
    templateUrl: './database-admin.component.html',
    styleUrls: ['./database-admin.component.scss']
})
export class DatabaseAdminComponent {

    itemsPerPage: number = 5;
    database: string = 'jdbc';
    paginate: boolean = false;
    statement: string = '';
    errorMessage: string;
    sqlResult = new Sqlresult();

    paginationInstanceConfig: PaginationInstance = {
        id: 'admin-pagination',
        currentPage: 1,
        itemsPerPage: this.itemsPerPage
    };

    constructor(private databaseAdminService: DatabaseAdminService) {
    }

    execute() {
        if (this.statement.length === 0) {
            return;
        }

        this.paginationInstanceConfig.itemsPerPage = this.itemsPerPage;

        this.errorMessage = null;
        if (this.paginate) {
            this.databaseAdminService.paginate(this.statement, this.database, this.paginationInstanceConfig.currentPage, this.paginationInstanceConfig.itemsPerPage)
                .subscribe(
                    (res: Array<Object>) => {
                        this.sqlResult = sqlResultFromObject(res['data'][0]);
                        this.paginationInstanceConfig.totalItems = res['totalCount'];
                    },
                    (error) => {
                        this.errorMessage = error.error;
                        this.sqlResult = new Sqlresult();
                    }
                );
        } else {
            this.databaseAdminService.execute(this.statement, this.database)
                .subscribe(
                    (res: Array<Object>) => {
                        this.sqlResult = sqlResultFromObject(res);
                    },
                    (error) => {
                        this.errorMessage = error.error;
                        this.sqlResult = new Sqlresult();
                    }
                );
        }
    }

    onPaginationChange() {
        if (this.paginate) {
            this.execute();
        }
    }

    pageChange(event: number) {
        this.paginationInstanceConfig.currentPage = event;
        this.execute();
    }
}
