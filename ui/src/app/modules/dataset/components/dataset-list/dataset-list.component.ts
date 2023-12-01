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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { newInstance } from '@shared/tools';
import { distinct, flatMap } from '@shared/tools/array-utils';
import { DataSetService } from '@core/services';
import { Dataset, Authorization } from '@model';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import { FeatureName } from '@core/feature/feature.model';
import { FeatureService } from '@core/feature/feature.service';

@Component({
    selector: 'chutney-dataset-list',
    templateUrl: './dataset-list.component.html',
    styleUrls: ['./dataset-list.component.scss']
})
export class DatasetListComponent implements OnInit, OnDestroy {

    datasets: Array<Dataset> = [];

    preview: Dataset = null;

    dataSetFilter = '';
    itemList = [];
    settings = {};
    selectedTags: string[] = [];
    selectedItem: any[];
    urlParams: Subscription;

    Authorization = Authorization;

    constructor(
        private router: Router,
        private dataSetService: DataSetService,
        private readonly route: ActivatedRoute
    ) {}

    ngOnInit(): void {
        this.dataSetService.findAll().subscribe(
            (res) => {
                this.datasets = res;
                this.initTags();
                this.applyUriState();
            },
            (error) => console.log(error)
        );

        this.settings = {
            enableCheckAll: false,
            autoPosition: false
        };
    }

    ngOnDestroy(): void {
        if (this.urlParams) {
            this.urlParams.unsubscribe();
        }
    }

    showPreview(dataset: Dataset) {
        if (this.preview == null || this.preview.id !== dataset.id) {
            this.dataSetService.findById(dataset.id).subscribe(
                (res) => {
                    this.preview = res;
                },
                (error) => console.log(error)
            );
        } else {
            this.preview = null;
        }
    }

    private initTags() {
        const allTagsInDataset: string[] = distinct(flatMap(this.datasets, (sc) => sc.tags)).sort();
        let index = 0;
        this.itemList = allTagsInDataset.map(t => {
            index++;
            return { 'id': index, 'itemName': t };
        });
    }

    filterSearchChange(searchFilter: string) {
        this.dataSetFilter = searchFilter;
        this.applyFiltersToRoute();
    }

    onItemSelect(item: any) {
        this.selectedTags.push(item.itemName);
        this.selectedTags = newInstance(this.selectedTags);
    }

    onItemDeSelect(item: any) {
        this.selectedTags.splice(this.selectedTags.indexOf(item.itemName), 1);
        this.selectedTags = newInstance(this.selectedTags);
    }

    onItemDeSelectAll() {
        this.selectedTags = newInstance([]);
    }

    applyFiltersToRoute() {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {
                text: this.dataSetFilter ? this.dataSetFilter : null,
                tags: this.selectedItem?.length ? this.selectedItem.map((i) => i.itemName).toString() : null
            }
        });
    }

    private applyUriState() {
        this.urlParams = this.route.queryParams
            .pipe(map((params: Array<any>) => {
                    if (params['text']) {
                        this.dataSetFilter = params['text'];
                    }
                    if (params['tags']) {
                        const uriTag = params['tags'].split(',');
                        if (uriTag != null) {
                            this.selectedItem = this.itemList.filter((tagItem) => uriTag.includes(tagItem.itemName));
                            this.selectedTags = this.selectedItem.map((i) => i.itemName);
                            this.applyFiltersToRoute();
                        }
                    }
                }))
            .subscribe();
    }
}
