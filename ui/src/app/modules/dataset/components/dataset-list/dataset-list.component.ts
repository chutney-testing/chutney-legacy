import { Component, OnInit } from '@angular/core';
import { newInstance } from '@shared/tools';
import { DataSetService } from '@core/services';
import { Dataset } from '@core/model';
import { distinct, flatMap } from '@shared/tools/array-utils';

@Component({
    selector: 'chutney-dataset-list',
    templateUrl: './dataset-list.component.html',
    styleUrls: ['./dataset-list.component.scss']
})
export class DatasetListComponent implements OnInit {

    datasets: Array<Dataset> = [];

    preview: Dataset;

    dataSetFilter = '';
    itemList = [];
    settings = {};
    selectedTags: string[] = [];
    selectedItem: any[];

    constructor(private dataSetService: DataSetService) {
    }

    ngOnInit(): void {
        this.dataSetService.findAll().subscribe(
            (res) => {
                this.datasets = res;
                this.initTags();
            },
            (error) => console.log(error)
        );

        this.settings = {
            enableCheckAll: false,
            autoPosition: false
        };
    }

    showPreview(dataset: Dataset) {
        this.dataSetService.findById(dataset.id).subscribe(
            (res) => {
                this.preview = res;
            },
            (error) => console.log(error)
        );
    }

    private initTags() {
        const allTagsInDataset: string[] =  distinct(flatMap(this.datasets, (sc) => sc.tags)).sort();
        let index = 0;
        this.itemList = allTagsInDataset.map(t => {
            index++;
            return { 'id': index, 'itemName': t };
        });
    }

    filterSearchChange(searchFilter: string) {
        this.dataSetFilter = searchFilter;
    }

    onItemSelect(item: any) {
        this.selectedTags.push(item.itemName);
        this.selectedTags = newInstance(this.selectedTags);
    }

    onItemDeSelect(item: any) {
        this.selectedTags.splice(this.selectedTags.indexOf(item.itemName), 1);
        this.selectedTags =  newInstance(this.selectedTags);
    }
}
