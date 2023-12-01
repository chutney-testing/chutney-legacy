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

import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { delay } from '@shared/tools';
import { CanDeactivatePage } from '@core/guards';
import { DataSetService } from '@core/services';
import { ValidationService } from '../../../../molecules/validation/validation.service';
import { Dataset, KeyValue } from '@model';

@Component({
    selector: 'chutney-dataset-edition',
    templateUrl: './dataset-edition.component.html',
    styleUrls: ['./dataset-edition.component.scss']
})
export class DatasetEditionComponent extends CanDeactivatePage implements OnInit, OnDestroy, AfterViewInit {

    dataset: Dataset = new Dataset('', '', [], new Date(), [], [], 0);

    activeTab = 'keyValue';
    datasetForm: FormGroup;
    private routeParamsSubscription: Subscription;
    private previousDataSet: Dataset = this.dataset;
    private modificationsSaved = false;
    message;
    backendError;
    private savedMessage: string;
    errorDuplicateHeader = false;

    errorDuplicateHeaderMessage: string
    @ViewChild('dataSetName') dataSetName: ElementRef;

    constructor(private dataSetService: DataSetService,
                private router: Router,
                private route: ActivatedRoute,
                private validationService: ValidationService,
                private translate: TranslateService,
                private formBuilder: FormBuilder,
                private location: Location) {
        super();
    }

    ngOnInit(): void {
        this.datasetForm = this.formBuilder.group({
            name: ['', Validators.required],
            description: '',
            tags: [],
            keyValues: new FormControl(),
            multiKeyValues: new FormControl()
        });

        this.routeParamsSubscription = this.route.params.subscribe((params) => {
            this.load(params['id']);
        });

        this.initTranslation();

    }

    ngAfterViewInit(): void {
        this.dataSetNameFocus();
    }

    private initTranslation() {
        this.translate.get('global.actions.done.saved').subscribe((res: string) => {
            this.savedMessage = res;
        });
        this.translate.get('components.dataset.error.duplicatedHeader').subscribe((res: string) => {
            this.errorDuplicateHeaderMessage = res;
        });
    }

    ngOnDestroy() {
        this.routeParamsSubscription.unsubscribe();
    }

    load(id) {
        if (id != null) {
            this.dataSetService.findById(id).subscribe(
                (res) => {
                    this.setCurrentDataSet(res);
                }
            );
        }
    }

    private setCurrentDataSet(res) {
        this.dataset = res;
        this.previousDataSet = res;
        this.datasetForm.controls['name'].patchValue(this.dataset.name);
        this.datasetForm.controls['description'].patchValue(this.dataset.description);
        this.datasetForm.controls['tags'].patchValue(this.dataset.tags.join(','));
        this.datasetForm.controls['keyValues'].patchValue(this.dataset.uniqueValues);
        this.datasetForm.controls['multiKeyValues'].patchValue(this.dataset.multipleValues);
    }

    isValid(): boolean {
        return this.validationService.isNotEmpty(this.datasetForm.value['name']);
    }

    save() {
        const dataset = this.createDataset();
        this.errorDuplicateHeader = false;
        this.dataSetService.save(dataset, this.previousDataSet.id)
            .subscribe({
                next: (res) => {
                    this.setCurrentDataSet(res);
                    this.location.replaceState('/dataset/' + this.dataset.id + '/edition');
                    this.notify(this.savedMessage, null);
                    this.modificationsSaved = true;
                },
                error: (error) => {
                    if (error.status === 400) {
                        this.errorDuplicateHeader = true;
                        this.notify(this.errorDuplicateHeaderMessage + ':', error.error);
                        this.modificationsSaved = false;
                    } else {
                        this.notify(null, error.error);
                        this.modificationsSaved = false;
                    }
                }
            });
    }

    notify(message: string, backendError: string) {
        (async () => {
            this.message = message;
            this.backendError = backendError;
            await delay(5000);
            this.message = null;
            this.backendError = null;
        })();
    }

    canDeactivatePage(): boolean {
        return this.modificationsSaved || this.createDataset().equals(this.previousDataSet);
    }

    cancel() {
        this.location.back();
    }

    selectTab(tab: string) {
        this.activeTab = tab;
    }

    deleteDataset() {
        this.dataSetService.delete(this.dataset.id).subscribe(
            () => {
                this.modificationsSaved = true;
                this.router.navigateByUrl('/dataset');
            },
            error => console.log(error));
    }

    private createDataset() {
        const name = this.datasetForm.value['name'] ? this.datasetForm.value['name'] : '';
        const desc = this.datasetForm.value['description'] ? this.datasetForm.value['description'] : '';
        const tags = this.datasetForm.value['tags'] ? this.datasetForm.value['tags'].split(',') : [];
        const date = new Date();

        const kv = this.datasetForm.controls['keyValues'] as FormArray;
        const keyValues = kv.value ? kv.value.map((p) => new KeyValue(p.key, p.value)) : [];

        const mkv = this.datasetForm.controls['multiKeyValues'] as FormArray;
        const multiKeyValues = mkv.value ? mkv.value.map(a => a.map((p) => new KeyValue(p.key, p.value))) : [];

        const version = this.dataset.id ? this.dataset.version : 0;
        const id = this.dataset.id ? this.dataset.id : null;

        return new Dataset(
            name,
            desc,
            tags,
            date,
            keyValues,
            multiKeyValues,
            version,
            id
        );
    }

    private dataSetNameFocus(): void {
        if (this.dataset.id == null || this.dataset.id.length === 0) {
            this.dataSetName.nativeElement.focus();
        }
    }
}
