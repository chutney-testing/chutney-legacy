import { Component } from '@angular/core';
import { newInstance } from '@shared/tools';
import { DataSetService } from '@core/services';
import { Dataset } from '@core/model';

@Component({
    selector: 'chutney-dataset-list',
    templateUrl: './dataset-list.component.html',
    styleUrls: ['./dataset-list.component.scss']
})
export class DatasetListComponent {

    datasets: Array<Dataset> = [];

    dataSetFilter = '';
    itemList = [];
    settings = {};
    selectedTags: string[] = [];

    constructor(private dataSetService: DataSetService) {
    }

    createDataSet() {
        // TODO redirect to create dataset
    }

    filterSearchChange(searchFilter: string) {
        this.dataSetFilter = searchFilter;
    }

    onItemSelect(item: any) {
        this.selectedTags.push(item.itemName);
        this.selectedTags = newInstance(this.selectedTags);
    }

    OnItemDeSelect(item: any) {
        this.selectedTags.splice(this.selectedTags.indexOf(item.itemName), 1);
        this.selectedTags =  newInstance(this.selectedTags);
    }


}
