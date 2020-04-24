import { Component, OnInit } from '@angular/core';
import { DataSetService } from '@core/services';

@Component({
    selector: 'chutney-dataset-edition',
    templateUrl: './dataset-edition.component.html',
    styleUrls: ['./dataset-edition.component.scss']
})
export class DatasetEditionComponent implements OnInit {

    constructor(private dataSetService: DataSetService) {
    }

    ngOnInit(): void {

    }


}
