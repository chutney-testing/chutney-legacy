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

import { Component, HostListener, SimpleChanges, Input, OnChanges, OnDestroy } from '@angular/core';

import { EditionService } from '@core/services';
import { TestCaseEdition } from '@model';

@Component({
    selector: 'chutney-edition-info',
    templateUrl: './edition-info.component.html',
    styleUrls: ['./edition-info.component.scss']
})
export class EditionInfoComponent implements OnChanges, OnDestroy {
    @Input() testCase;

    edition: TestCaseEdition;
    editions: Array<TestCaseEdition> = [];

    constructor(private editionService: EditionService) {
    }

    ngOnChanges(changes: SimpleChanges) {
        if (this.testCase && this.testCase.id) {
            const id = this.testCase.id;
            this.editionService.editTestCase(id).subscribe(
                edition => {
                    this.edition = edition;
                    this.editionService.findAllTestCaseEditions(id).subscribe(
                        editions => { this.editions = editions.filter(e => e.editionUser != edition.editionUser); }
                    );
                },
                error => {
                    console.log(error);
                }
            );
        }
    }

    @HostListener('window:beforeunload')
    async ngOnDestroy() {
        if (this.testCase.id != null) {
            await this.editionService.endTestCaseEdition(this.testCase.id)
                .toPromise();
        }
    }
}
