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
