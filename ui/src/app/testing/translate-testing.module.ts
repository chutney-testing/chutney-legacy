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
