import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Execution } from '@model';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { ScenarioExecutionService } from '@modules/scenarios/services/scenario-execution.service';
import { ExecutionStatus } from '@core/model/scenario/execution-status';
import { FormBuilder, FormGroup } from '@angular/forms';
import { debounceTime, switchMap } from 'rxjs/operators';
import { Observable, Subscription } from 'rxjs';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { DateFormatPipe } from 'ngx-moment';
import { AngularMultiSelect } from 'angular2-multiselect-dropdown';
import { NgbDate } from '@ng-bootstrap/ng-bootstrap/datepicker/ngb-date';

@Component({
    selector: 'chutney-scenario-executions',
    templateUrl: './scenario-executions.component.html',
    styleUrls: ['./scenario-executions.component.scss']
})
export class ScenarioExecutionsComponent implements OnInit, OnDestroy {
    ExecutionStatus = ExecutionStatus;
    executions: Execution[] = [];
    filteredExecutions: Execution[] = [];

    filters: FormGroup;
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

    @ViewChild('statusDropdown',{static:false}) statusDropdown: AngularMultiSelect;
    @ViewChild('envsDropdown',{static:false}) envsDropdown: AngularMultiSelect;
    @ViewChild('executorsDropdown',{static:false}) executorsDropdown: AngularMultiSelect;
    @ViewChild('campsDropdown',{static:false}) campsDropdown: AngularMultiSelect;

    constructor(private scenarioExecutionService: ScenarioExecutionService,
                private route: ActivatedRoute,
                private router: Router,
                private formBuilder: FormBuilder,
                private datePipe: DateFormatPipe) {
    }

    ngOnInit(): void {
        this.getExecutions()
            .subscribe(executions => {
                this.executions = executions;
                this.initFiltersOptions();
                this.applyUriFilters();
            })
    }

    ngOnDestroy(): void {
        this.filters$.unsubscribe();
    }

    goToExecutionReport(executionId: number) {
        this.router.navigate([executionId], {relativeTo: this.route} )
    }

    private getExecutions(): Observable<Execution[]> {
        return this.route.params.pipe(
            switchMap(params => this.scenarioExecutionService.findScenarioExecutions(params['id']))
        );
    }

    private initFiltersOptions() {
        this.status = [...new Set(this.executions.map(exec => exec.status))].map(status => this.toSelectOption(status, ExecutionStatus.toString(status)));
        this.environments = [...new Set(this.executions.map(exec => exec.environment))].map(env => this.toSelectOption(env));
        this.executors = [...new Set(this.executions.map(exec => exec.user))].map(user => this.toSelectOption(user));
        this.campaigns = [...new Set(this.executions.filter(exec => !!exec.campaign).map(exec => exec.campaign))].map(camp => this.toSelectOption(camp));
    }

    private applyUriFilters() {
        this.route.queryParams.subscribe(params => {
            this.initFiltersForm(params);
            this.filteredExecutions = this.executions.filter(exec => this.matches(exec, this.filters.value))
        })

    }


    private initFiltersForm(params: Params) {
        this.filters = this.formBuilder.group({
            keyword: params['keyword'],
            date: this.formBuilder.control(this.toNgbDate(params['date'])),
            status: this.formBuilder.control(this.selectedOptionsFromUri(params['status'], ExecutionStatus.toString)),
            environments: this.formBuilder.control(this.selectedOptionsFromUri(params['env'])),
            executors: this.formBuilder.control(this.selectedOptionsFromUri(params['exec'])),
            campaigns: this.formBuilder.control(this.selectedOptionsFromUri(params['camp'])),
        });
        this.filters$ = this.filters
            .valueChanges
            .pipe(debounceTime(500))
            .subscribe(() => {
                this.router.navigate([], {
                    relativeTo: this.route,
                    queryParams: this.toQueryParams(this.filters.value)
                })
            });
    }



    private selectedOptionsFromUri(param: string, labelResolver: (id) => string = () => param) {
        if (param) {
            return param.split(',').map(part => this.toSelectOption(part, labelResolver(part)));
        }
        return [];

    }

    private toSelectOption(id: string, label?: string) {
       return {id: id, itemName: label ? label : id};
    }

    private toQueryParams(filters: any): Params {
        const params: Params = {};
        if (filters.keyword) {
            params['keyword'] = filters.keyword;
        }
        if (filters.status) {
            params['status'] = filters.status.map(status => status.id).toString();
        }
        if (filters.date) {
            params['date'] = this.formatToIsoDate(filters.date);
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

    private formatToIsoDate(ngbDate: NgbDateStruct) {
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
                + space;
            if (exec.campaign) {
                searchScope += space + exec.campaign;
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
            campaignMatch = !!filters.campaigns.find(camp => exec.campaign && camp.id === exec.campaign);
        }

        return keywordMatch && statusMatch && dateMatch && userMatch && envMatch && campaignMatch;
    }

    toggleDropDown(dropDown: AngularMultiSelect, event) {
        event.stopPropagation();
        dropDown.toggleDropdown(event);
    }

    getDateFilterValue() {
        let date: NgbDateStruct = this.filters.value.date;
        return new Date(date.year, date.month - 1, date.day);
    }


    noExecutionAt()  {
        return (date: NgbDate)=> !this.executions.filter(exec => this.matches(exec, {date: date})).length;
    }
}
