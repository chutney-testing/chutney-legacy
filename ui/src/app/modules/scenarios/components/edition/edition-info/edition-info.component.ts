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
