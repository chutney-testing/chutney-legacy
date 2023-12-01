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
import { Execution } from '@model';
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
    selector: 'chutney-scenario-executions',
    templateUrl: './scenario-executions.component.html',
    styleUrls: ['./scenario-executions.component.scss']
})
export class ScenarioExecutionsComponent implements OnChanges, OnDestroy {
    ExecutionStatus = ExecutionStatus;
    filteredExecutions: Execution[] = [];
    filtersForm: FormGroup;

    status: { id: string, itemName: string }[] = [];
    environments: { id: string, itemName: string }[] = [];
    executors: { id: string, itemName: string }[] = [];
    campaigns: { id: string, itemName: string }[] = [];
    selectSettings = {
        text: '',
        enableCheckAll: false,
        enableSearchFilter: true,
        autoPosition: false,
        classes: 'dropdown-list1'
    };

    private filters$: Subscription;

    private readonly iso_Date_Delimiter = '-';
    @ViewChild('statusDropdown', {static: false}) statusDropdown: AngularMultiSelect;

    @ViewChild('envsDropdown', {static: false}) envsDropdown: AngularMultiSelect;
    @ViewChild('executorsDropdown', {static: false}) executorsDropdown: AngularMultiSelect;
    @ViewChild('campsDropdown', {static: false}) campsDropdown: AngularMultiSelect;

    @Input() executions: Execution[] = [];
    @Output() onExecutionSelect = new EventEmitter<{ execution: Execution, focus: boolean }>();
    @Input() filters: Params;
    @Output() filtersChange = new EventEmitter<Params>();

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

    toggleDropDown(dropDown: AngularMultiSelect, event) {
        event.stopPropagation();
        dropDown.toggleDropdown(event);
    }

    getDateFilterValue() {
        let date: NgbDateStruct = this.filtersForm.value.date;
        return new Date(date.year, date.month - 1, date.day);
    }

    noExecutionAt() {
        return (date: NgbDate) => !this.executions.filter(exec => this.matches(exec, {date: date})).length;
    }

    openReport(execution: Execution, focus: boolean = true) {
        this.onExecutionSelect.emit({execution, focus});
    }

    ngOnDestroy(): void {
        this.filters$.unsubscribe();
    }


    private initFiltersOptions() {
        this.status = [...new Set(this.executions.map(exec => exec.status))].map(status => this.toSelectOption(status,  this.translateService.instant(ExecutionStatus.toString(status))));
        this.environments = [...new Set(this.executions.map(exec => exec.environment))].map(env => this.toSelectOption(env));
        this.executors = [...new Set(this.executions.map(exec => exec.user))].map(user => this.toSelectOption(user));
        this.campaigns = [...new Set(this.executions.filter(exec => !!exec.campaignReport).map(exec => exec.campaignReport.campaignName))].map(camp => this.toSelectOption(camp));
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
            executors: this.formBuilder.control(this.selectedOptionsFromUri(this.filters['exec'])),
            campaigns: this.formBuilder.control(this.selectedOptionsFromUri(this.filters['camp'])),
        });
    }

    private applyFiltersOnExecutions() {
        this.filteredExecutions = this.executions.filter(exec => this.matches(exec, this.filtersForm.value))
    }

    private onFiltersChange() {
        this.filters$ = this.filtersForm
            .valueChanges
            .pipe(
                debounceTime(500),
                map(value => this.toQueryParams(value)),
                tap(params => this.filtersChange.emit(params)))
            .subscribe();
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
        if (filters.campaigns && filters.campaigns.length) {
            params['camp'] = filters.campaigns.map(env => env.id).toString();
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

    private matches(exec: Execution, filters: any): boolean {
        let keywordMatch = true;
        if (filters.keyword) {
            let space = ' ';
            let searchScope = exec.user
                + space
                + exec.environment
                + space
                + this.datePipe.transform(exec.time, 'DD MMM. YYYY HH:mm')
                + space
                + exec.executionId
                + space
                + this.translateService.instant(ExecutionStatus.toString(exec.status))
                + space;
            if (exec.campaignReport) {
                searchScope += space + exec.campaignReport.campaignName;
            }

            if (exec.error) {
                searchScope += space + exec.error;
            }
            keywordMatch = searchScope.toLowerCase().includes(filters.keyword.toLowerCase());
        }
        let statusMatch = true;
        if (filters.status && filters.status.length) {
            statusMatch = !!filters.status.find(status => status.id === exec.status);
        }
        let dateMatch = true;
        if (filters.date) {
            const dateFilter = new Date(filters.date.year, filters.date.month - 1, filters.date.day);
            dateMatch = dateFilter.toDateString() === new Date(exec.time).toDateString();
        }

        let userMatch = true;
        if (filters.executors && filters.executors.length) {
            userMatch = !!filters.executors.find(executor => executor.id === exec.user);
        }

        let envMatch = true;
        if (filters.environments && filters.environments.length) {
            envMatch = !!filters.environments.find(env => env.id === exec.environment);
        }

        let campaignMatch = true;
        if (filters.campaigns && filters.campaigns.length) {
            campaignMatch = !!filters.campaigns.find(camp => exec.campaignReport && camp.id === exec.campaignReport.campaignName);
        }

        return keywordMatch && statusMatch && dateMatch && userMatch && envMatch && campaignMatch;
    }

    openCampaignExecution(execution: Execution, event: MouseEvent) {
        if (execution.campaignReport) {
            event.stopPropagation();
            this.router.navigate(['/campaign', execution.campaignReport.campaignId, 'executions'], {queryParams: {open: execution.campaignReport.executionId, active: execution.campaignReport.executionId}});
        }

    }
}
