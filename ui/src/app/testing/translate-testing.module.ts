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

import { TranslateModule, TranslateLoader, TranslatePipe, TranslateService } from '@ngx-translate/core';
import { NgModule, PipeTransform, Pipe, Injectable, EventEmitter } from '@angular/core';
import { Observable, of } from 'rxjs';

const TRANSLATIONS_EN = require('src/assets/i18n/en.json');

export class FakeLoader implements TranslateLoader {
    getTranslation(lang: string): Observable<any> {
        return of(TRANSLATIONS_EN);
    }
}

@Pipe({
    name: 'translate'
})
export class TranslatePipeMock implements PipeTransform {
    public name = 'translate';

    public transform(query: string, ...args: any[]): any {
        return fromKey(query);
    }
}

@Injectable()
export class TranslateServiceStub {
    public get(key: string): Observable<string> {
        return of(fromKey(key));
    }
    public onLangChange: EventEmitter<any> = new EventEmitter();
    public onTranslationChange: EventEmitter<any> = new EventEmitter();
    public onDefaultLangChange: EventEmitter<any> = new EventEmitter();
}

function fromKey(key: string): string {
    let r = TRANSLATIONS_EN;
    key.split('.').forEach((s) => { if (r) r = r[s]; });
    if (r) { return r; } else { return key; }
}

@NgModule({
    declarations: [
        TranslatePipeMock
    ],
    providers: [
        { provide: TranslateService, useClass: TranslateServiceStub },
        { provide: TranslatePipe, useClass: TranslatePipeMock },
    ],
    imports: [
        TranslateModule.forRoot({
            loader: { provide: TranslateLoader, useClass: FakeLoader },
        })
    ],
    exports: [
        TranslatePipeMock,
        TranslateModule
    ]
})
export class TranslateTestingModule {

}
