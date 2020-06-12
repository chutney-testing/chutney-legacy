import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';
import { DataSetService } from '@core/services';
import { Dataset, ScenarioComponent, TestCase } from '@model';


@Component({
    selector: 'chutney-dataset-selection',
    templateUrl: './dataset-selection.component.html',
    styleUrls: ['./dataset-selection.component.scss']
})
export class DatasetSelectionComponent implements OnInit {

    @Input() selectedDatasetId: String;
    @Output() selectionEvent = new EventEmitter();

    private datasets: Array<Dataset>;

    constructor(private datasetService: DataSetService) {
    }

    ngOnInit(): void {
        this.datasetService.findAll().subscribe((res: Array<Dataset>) => {
            this.datasets = res;
        });
    }

    changingValue(event: any) {
        this.selectionEvent.emit(event.target.value);
    }
}
