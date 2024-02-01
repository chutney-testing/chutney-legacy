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
import { Campaign } from '@core/model';
import { CampaignService } from '@core/services';
import { CampaignScheduling } from '@core/model/campaign/campaign-scheduling.model';
import { CampaignSchedulingService } from '@core/services/campaign-scheduling.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbDatepickerConfig, NgbDateStruct, NgbTimepickerConfig } from '@ng-bootstrap/ng-bootstrap';
import { NgbDate } from '@ng-bootstrap/ng-bootstrap/datepicker/ngb-date';
import { NgbTime } from '@ng-bootstrap/ng-bootstrap/timepicker/ngb-time';
import { FREQUENCY } from '@core/model/campaign/FREQUENCY';
import { DragulaService } from "ng2-dragula";
import { AngularMultiSelect } from 'angular2-multiselect-dropdown';

@Component({
    selector: 'chutney-campaign-scheduling',
    templateUrl: './campaign-scheduling.component.html',
    styleUrls: ['./campaign-scheduling.component.scss']
})
export class CampaignSchedulingComponent implements OnInit {

    scheduledCampaigns: Array<CampaignScheduling> = [];
    form: FormGroup;
    errorMessage: string;
    submitted: boolean;
    frequencies = Object.values(FREQUENCY);
    campaigns: Array<Campaign> = [];
    settings = {};

    public selectedMoment = new Date();

    model: NgbDateStruct;

    constructor(private campaignSchedulingService: CampaignSchedulingService,
                private campaignService: CampaignService,
                private dragulaService: DragulaService,
                private formBuilder: FormBuilder,
                private configTime: NgbTimepickerConfig,
                private configDate: NgbDatepickerConfig
    ) {
        this.configTime.spinners = false;
        const currentDate = new Date();
        this.configDate.minDate = {
            year: currentDate.getFullYear(),
            month: currentDate.getMonth() + 1,
            day: currentDate.getDate()
        };
        this.configDate.maxDate = {year: currentDate.getFullYear() + 1, month: 12, day: 31};
    }

    ngOnInit() {
        this.campaignService.findAllCampaigns().subscribe(
            (res) => {
                this.campaigns = res;
            },
            (error) => {
                this.errorMessage = 'Cannot get campaign list - ' + error;
            });

        this.loadSchedulingCampaign();


        this.form = this.formBuilder.group({
            selectedCampaigns: [[], Validators.required],
            date: ['', Validators.required],
            time: ['', Validators.required],
            frequency: ['']
        });

        this.settings = {
            text: '',
            enableCheckAll: false,
            enableSearchFilter: true,
            autoPosition: false,
            showCheckbox: false,
            labelKey: 'title'
        };
    }

    create() {
        this.submitted = true;
        const formValue = this.form.value;
        if (this.form.invalid) {
            return;
        }

        const date: NgbDate = formValue['date'];
        const time: NgbTime = formValue['time'];
        const campaignList: Array<Campaign> = this.form.get('selectedCampaigns').value;
        const dateTime = new Date(date.year, date.month - 1, date.day, time.hour, time.minute, 0, 0);
        dateTime.setHours(time.hour - dateTime.getTimezoneOffset() / 60);
        const frequency: FREQUENCY = formValue['frequency'];
        const schedulingCampaign = new CampaignScheduling(
            campaignList.map(campaign => campaign.id),
            campaignList.map(campaign => campaign.title),
            dateTime, frequency
        );

        this.campaignSchedulingService.create(schedulingCampaign).subscribe({
            next: () => {
                this.loadSchedulingCampaign();
                this.form.reset();
            },
            error: (error) => {
                this.errorMessage = 'Cannot create - ' + error;
            }
        });

        this.submitted = false;
    }

    delete(id: number) {
        this.campaignSchedulingService.delete(id).subscribe({
            next: () => {
                this.loadSchedulingCampaign();
            },
            error: (error) => {
                this.errorMessage = 'Cannot delete - ' + error;
            }
        });
    }

    unselectCampaign(campaign: Campaign) {
        const selectedCampaigns = this.form.get('selectedCampaigns').value.filter( (c: Campaign) => c !== campaign);
        this.form.get('selectedCampaigns').setValue([...selectedCampaigns]);
    }

    toggleDropDown(dropDown: AngularMultiSelect, event) {
        event.stopPropagation();
        dropDown.toggleDropdown(event);
    }

    private loadSchedulingCampaign() {
        this.campaignSchedulingService.findAll().subscribe({
            next: (res) => {
                this.scheduledCampaigns = res;
            },
            error: (error) => {
                this.errorMessage = 'Cannot get scheduled campaigns - ' + error;
            }
        });
    }

    // convenience getter for easy access to form fields
    get f() {
        return this.form.controls;
    }
}
