import { Component, OnInit } from '@angular/core';
import { Campaign } from '@core/model';
import { CampaignService } from '@core/services';
import { CampaignScheduling } from '@core/model/campaign/campaign-scheduling.model';
import { CampaignSchedulingService } from '@core/services/campaign-scheduling.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbDatepickerConfig, NgbDateStruct, NgbTimepickerConfig } from '@ng-bootstrap/ng-bootstrap';
import { NgbDate } from '@ng-bootstrap/ng-bootstrap/datepicker/ngb-date';
import { NgbTime } from '@ng-bootstrap/ng-bootstrap/timepicker/ngb-time';

@Component({
    selector: 'chutney-campaign-scheduling',
    templateUrl: './campaign-scheduling.component.html',
    styleUrls: ['./campaign-scheduling.component.scss']
})
export class CampaignSchedulingComponent implements OnInit {

    scheduledCampaigns: Array<CampaignScheduling> = [];
    form: FormGroup;
    errorMessage: string
    submitted: boolean;
    frequencies: Array<String> = ['hourly', 'daily', 'weekly', 'monthly'];

    campaigns: Array<Campaign> = [];

    model: NgbDateStruct;

    constructor(private campaignSchedulingService: CampaignSchedulingService,
                private campaignService: CampaignService,
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
            campaign: [null, Validators.required],
            date: [null, Validators.required],
            time: [null, Validators.required],
            frequency: [null]
        });
    }

    create() {
        this.submitted = true;
        const formValue = this.form.value;
        if (this.form.invalid) {
            return;
        }

        const date: NgbDate = formValue['date'];
        const time: NgbTime = formValue['time'];
        const campaign: Campaign = this.form.get('campaign').value;
        const dateTime = new Date(date.year, date.month - 1, date.day, time.hour, time.minute, 0, 0);
        dateTime.setHours(time.hour - dateTime.getTimezoneOffset() / 60)
        const frequency: String = formValue['frequency'];

        const schedulingCampaign = new CampaignScheduling(
            campaign.id,
            campaign.title,
            dateTime, frequency
        )

        this.campaignSchedulingService.create(schedulingCampaign).subscribe(() => {
                this.loadSchedulingCampaign();
            },
            (error) => {
                this.errorMessage = 'Cannot create - ' + error;
            });

        this.submitted = false;

    }

    delete(id: number) {
        this.campaignSchedulingService.delete(id).subscribe(
            () => {
                this.loadSchedulingCampaign();
            },
            (error) => {
                this.errorMessage = 'Cannot delete - ' + error;
            });
    }


    private loadSchedulingCampaign() {
        this.campaignSchedulingService.findAll().subscribe(
            (res) => {
                this.scheduledCampaigns = res;
            },
            (error) => {
                this.errorMessage = 'Cannot get scheduled campaigns - ' + error;
            });
    }

    // convenience getter for easy access to form fields
    get f() {
        return this.form.controls;
    }
}
