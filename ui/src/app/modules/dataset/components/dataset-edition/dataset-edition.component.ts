import { Component, OnDestroy, OnInit } from '@angular/core';
import { DataSetService } from '@core/services';
import { ValidationService } from '../../../../molecules/validation/validation.service';
import { Dataset, KeyValue } from '@model';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CanDeactivatePage } from '@core/guards';
import { delay } from '@shared/tools';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'chutney-dataset-edition',
    templateUrl: './dataset-edition.component.html',
    styleUrls: ['./dataset-edition.component.scss']
})
export class DatasetEditionComponent extends CanDeactivatePage implements OnInit, OnDestroy {

    dataset: Dataset = new Dataset('', '', [], new Date(), [], [], 0);

    activeTab = 'keyValue';
    datasetForm: FormGroup;
    private routeParamsSubscription: Subscription;
    private previousDataSet: Dataset;
    private modificationsSaved: boolean = false;
    message;
    private savedMessage: string;

    constructor(private dataSetService: DataSetService,
                private router: Router,
                private route: ActivatedRoute,
                private validationService: ValidationService,
                private translate: TranslateService,
                private formBuilder: FormBuilder) {
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

    private initTranslation() {
        this.translate.get('global.actions.done.saved').subscribe((res: string) => {
            this.savedMessage = res;
        });
    }

    ngOnDestroy() {
        this.routeParamsSubscription.unsubscribe();
    }

    load(id) {
        if (id !== undefined) {
            this.dataSetService.findById(id).subscribe(
                (res) => {
                    this.dataset = res;
                    this.datasetForm.controls.name.patchValue(this.dataset.name);
                    this.datasetForm.controls.description.patchValue(this.dataset.description);
                    this.datasetForm.controls.tags.patchValue(this.dataset.tags.join(', '));
                    this.datasetForm.controls.keyValues.patchValue(this.dataset.uniqueValues);
                    this.datasetForm.controls.multiKeyValues.patchValue(this.dataset.multipleValues);
                }
            );

        }
    }

    isValid(): boolean {
        return this.validationService.isNotEmpty(this.datasetForm.value['name']);
    }

    save() {
        const dataset = this.createDataset();
        this.dataSetService.save(dataset)
            .subscribe( (res) => {
                this.modificationsSaved = true;
                this.previousDataSet = dataset;
                this.notify(this.savedMessage);
                this.dataset = res;
            });
    }

    notify(message: string) {
        (async () => {
            this.message = message;
            await delay(3000);
            this.message = null;
        })();
    }

    canDeactivatePage(): boolean {
        return this.modificationsSaved || this.createDataset().equals(this.previousDataSet);
    }

    cancel() {
        this.router.navigateByUrl('/dataset');
    }

    selectTab(tab: string) {
        this.activeTab = tab;
    }

    deleteDataset() {
        this.dataSetService.delete(this.dataset.id).subscribe(
            () => this.router.navigateByUrl('/dataset'),
            error => console.log(error));
    }

    private createDataset() {
        const name = this.datasetForm.value['name'] ? this.datasetForm.value['name'] : '';
        const desc = this.datasetForm.value['description'] ? this.datasetForm.value['description'] : '';
        const tags = this.datasetForm.value['tags'] ? this.datasetForm.value['tags'].split(',') : [];
        const date = new Date();

        let kv = this.datasetForm.controls.keyValues as FormArray;
        const keyValues = kv.value ? kv.value.map((kv) => new KeyValue(kv.key, kv.value)) : [];

        let mkv = this.datasetForm.controls.multiKeyValues as FormArray;
        const multiKeyValues = mkv.value ? mkv.value : [];

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
}
