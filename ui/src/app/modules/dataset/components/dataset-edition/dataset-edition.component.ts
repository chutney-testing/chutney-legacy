import { Component, OnDestroy, OnInit } from '@angular/core';
import { DataSetService } from '@core/services';
import { ValidationService } from '../../../../molecules/validation/validation.service';
import { Dataset, KeyValue } from '@model';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
    selector: 'chutney-dataset-edition',
    templateUrl: './dataset-edition.component.html',
    styleUrls: ['./dataset-edition.component.scss']
})
export class DatasetEditionComponent implements OnInit, OnDestroy {

    dataset: Dataset = new Dataset('', '', [], new Date(), [], [], 0);

    activeTab = 'multiKeyValue';
    datasetForm: FormGroup;
    private routeParamsSubscription: Subscription;

    dataGridMock: Array<KeyValue> = [];

    constructor(private dataSetService: DataSetService,
                private router: Router,
                private route: ActivatedRoute,
                private validationService: ValidationService,
                private formBuilder: FormBuilder) {
    }

    ngOnInit(): void {
        let values: string[] = ['X', 'Y', 'Z'];
        let values2: string[] = ['X2', 'Y2', 'Z2'];
        let values3: string[] = ['X3', 'Y3', 'Z3'];
        let columns = new KeyValue('A', values);
        let columns2 = new KeyValue('B', values2);
        let columns3 = new KeyValue('C', values3);

        this.dataGridMock.push(columns);
        this.dataGridMock.push(columns2);
        this.dataGridMock.push(columns3);

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
                }
            );

        }
    }

    isValid(): boolean {
        return this.validationService.isNotEmpty(this.datasetForm.value['name']);
    }

    confirm() {
        this.dataSetService.save(this.createDataset())
            .subscribe(() => this.router.navigateByUrl('/dataset'));
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
