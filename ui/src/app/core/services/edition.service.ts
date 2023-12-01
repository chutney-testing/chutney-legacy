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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '@env/environment';

import { TestCaseEdition } from '@model';

@Injectable({
    providedIn: 'root'
})
export class EditionService {

    private resourceTestCaseUrl = '/api/v1/editions/testcases';

    constructor(
        private httpClient: HttpClient) {
    }

    findAllTestCaseEditions(testCaseId: string): Observable<Array<TestCaseEdition>> {
        return this.httpClient.get<Array<TestCaseEdition>>(`${environment.backend}${this.resourceTestCaseUrl}/${testCaseId}`)
            .pipe(map((res: Array<TestCaseEdition>) => {
                return res.map(c => this.mapToTestCaseEdition(c));
            }));
    }

    editTestCase(testCaseId: string): Observable<TestCaseEdition> {
        return this.httpClient.post(`${environment.backend}${this.resourceTestCaseUrl}/${testCaseId}`, '')
        .pipe(map((res: TestCaseEdition) => {
            return this.mapToTestCaseEdition(res);
        }));
    }

    endTestCaseEdition(testCaseId: string): Observable<void> {
        return this.httpClient.delete(`${environment.backend}${this.resourceTestCaseUrl}/${testCaseId}`)
            .pipe(map(() => {}));
    }

    private mapToTestCaseEdition(jsonObject: any): TestCaseEdition {
        return new TestCaseEdition(
            jsonObject.testCaseId,
            jsonObject.testCaseVersion,
            jsonObject.editionStartDate,
            jsonObject.editionUser
        );
    }

    private mapToTestCaseEditionDto(testCaseEdition: TestCaseEdition): TestCaseEditionDto {
        return new TestCaseEditionDto(
            testCaseEdition.testCaseId,
            testCaseEdition.testCaseVersion,
            testCaseEdition.editionStartDate,
            testCaseEdition.editionUser
        );
    }
}

export class TestCaseEditionDto {
    constructor(
        public testCaseId: string,
        public testCaseVersion: number,
        public editionStartDate: Date,
        public editionUser: string) {
    }
}
