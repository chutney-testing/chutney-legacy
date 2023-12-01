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

import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges, ViewChild } from '@angular/core';
import { CampaignExecutionReport, CampaignReport } from '@model';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { ExecutionStatus } from '@core/model/scenario/execution-status';
import { FormBuilder, FormGroup } from '@angular/forms';
import { debounceTime, map, tap } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { DateFormatPipe } from 'ngx-moment';
import { AngularMultiSelect } from 'angular2-multiselect-dropdown';
import { NgbDate } from '@ng-bootstrap/ng-bootstrap/datepicker/ngb-date';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'chutney-campaign-executions',
    templateUrl: './campaign-executions.component.html',
    styleUrls: ['./campaign-executions.component.scss']
})
export class CampaignExecutionsComponent implements OnChanges, OnDestroy {

    @Input() executions: CampaignReport[] = [];
    @Output() onExecutionSelect = new EventEmitter<{ execution: CampaignReport, focus: boolean }>();
    @Input() filters: Params;
    @Output() filtersChange = new EventEmitter<Params>();

    ExecutionStatus = ExecutionStatus;

    filtersForm: FormGroup;
    private filters$: Subscription;
    filteredExecutions: CampaignReport[] = [];

    status: { id: string, itemName: string }[] = [];
    @ViewChild('statusDropdown', {static: false}) statusDropdown: AngularMultiSelect;

    environments: { id: string, itemName: string }[] = [];
    @ViewChild('envsDropdown', {static: false}) envsDropdown: AngularMultiSelect;

    executors: { id: string, itemName: string }[] = [];
    @ViewChild('executorsDropdown', {static: false}) executorsDropdown: AngularMultiSelect;

    selectSettings = {
        text: '',
        enableCheckAll: false,
        enableSearchFilter: true,
        autoPosition: false,
        classes: 'dropdown-list1'
    };

    private readonly iso_Date_Delimiter = '-';

    constructor(private route: ActivatedRoute,
                private router: Router,
                private formBuilder: FormBuilder,
                private datePipe: DateFormatPipe,
                private translateService: TranslateService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.initFiltersOptions();
        this.applyFilters();
        this.onFiltersChange();
    }

    ngOnDestroy(): void {
        this.filters$.unsubscribe();
    }

    toggleDropDown(dropDown: AngularMultiSelect, event) {
        event.stopPropagation();
        dropDown.toggleDropdown(event);
    }

    getDateFilterValue() {
        let date: NgbDateStruct = this.filtersForm.value.date;
        return new Date(date.year, date.month - 1, date.day);
    }

    noExecutionAt() {
        return (date: NgbDate) => !this.executions.filter(exec => this.matches(exec.report, {date: date})).length;
    }

    openReport(execution: CampaignReport, focus: boolean = true) {
        this.onExecutionSelect.emit({execution, focus});
    }

    private initFiltersOptions() {
        this.status = [...new Set(this.executions.map(exec => exec.report.status))].map(status => this.toSelectOption(status,  this.translateService.instant(ExecutionStatus.toString(status))));
        this.environments = [...new Set(this.executions.map(exec => exec.report.executionEnvironment))].map(env => this.toSelectOption(env));
        this.executors = [...new Set(this.executions.map(exec => exec.report.user))].map(user => this.toSelectOption(user));
    }

    private applyFilters() {
        this.applyFiltersOnHeaders();
        this.applyFiltersOnExecutions();
    }

    private applyFiltersOnHeaders() {
        this.filtersForm = this.formBuilder.group({
            keyword: this.filters['keyword'],
            date: this.formBuilder.control(this.toNgbDate(this.filters['date'])),
            status: this.formBuilder.control(this.selectedOptionsFromUri(this.filters['status'],  (status) => this.translateService.instant(ExecutionStatus.toString(status)))),
            environments: this.formBuilder.control(this.selectedOptionsFromUri(this.filters['env'])),
            executors: this.formBuilder.control(this.selectedOptionsFromUri(this.filters['exec']))
        });
    }

    private applyFiltersOnExecutions() {
        this.filteredExecutions = this.executions.filter(exec => this.matches(exec.report, this.filtersForm.value))
    }

    private onFiltersChange() {
        this.filters$ = this.filtersForm
            .valueChanges
            .pipe(
                debounceTime(500),
                map(value => this.toQueryParams(value)),
                tap(params => this.filtersChange.emit(params))
            ).subscribe();
    }

    private selectedOptionsFromUri(param: string, labelResolver?: (param) => string) {
        if (param) {
            return param
                .split(',')
                .map(part => this.toSelectOption(part, labelResolver ? labelResolver(part) : part));
        }
        return [];
    }

    private toSelectOption(id: string, label: string = id) {
        return {id: id, itemName: label };
    }

    private toQueryParams(filters: any): Params {
        const params: Params = {};
        if (filters.keyword) {
            params['keyword'] = filters.keyword;
        }
        if (filters.status && filters.status.length) {
            params['status'] = filters.status.map(status => status.id).toString();
        }
        if (filters.date) {
            params['date'] = this.toIsoDate(filters.date);
        }
        if (filters.environments && filters.environments.length) {
            params['env'] = filters.environments.map(env => env.id).toString();
        }
        if (filters.executors && filters.executors.length) {
            params['exec'] = filters.executors.map(env => env.id).toString();
        }
        return params;
    }

    private toIsoDate(ngbDate: NgbDateStruct) {
        let dd = String(ngbDate.day).padStart(2, '0');
        let mm = String(ngbDate.month).padStart(2, '0')
        let yyyy = ngbDate.year;
        return [yyyy, mm, dd].join(this.iso_Date_Delimiter);
    }

    private toNgbDate(isoString: string) {
        if (isoString) {
            const date = isoString.split('-');
            return {
                day: parseInt(date[2], 10),
                month: parseInt(date[1], 10),
                year: parseInt(date[0], 10)
            };
        }
        return null;
    }

    private matches(report: CampaignExecutionReport, filters: any): boolean {
        let keywordMatch = true;
        if (filters.keyword) {
            let space = ' ';
            let searchScope = report.user
                + space
                + report.executionEnvironment
                + space
                + this.datePipe.transform(report.startDate, 'DD MMM. YYYY HH:mm')
                + space
                + report.executionId
                + space
                + this.translateService.instant(ExecutionStatus.toString(report.status))
                + space;

            keywordMatch = searchScope.toLowerCase().includes(filters.keyword.toLowerCase());
        }

        let statusMatch = true;
        if (filters.status && filters.status.length) {
            statusMatch = !!filters.status.find(status => status.id === report.status);
        }

        let dateMatch = true;
        if (filters.date) {
            const dateFilter = new Date(filters.date.year, filters.date.month - 1, filters.date.day);
            dateMatch = dateFilter.toDateString() === new Date(report.startDate).toDateString();
        }

        let userMatch = true;
        if (filters.executors && filters.executors.length) {
            userMatch = !!filters.executors.find(executor => executor.id === report.user);
        }

        let envMatch = true;
        if (filters.environments && filters.environments.length) {
            envMatch = !!filters.environments.find(env => env.id === report.executionEnvironment);
        }

        return keywordMatch && statusMatch && dateMatch && userMatch && envMatch;
    }
}
