import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Linkifier } from '@model';

@Pipe({name: 'linkify'})
export class LinkifyPipe implements PipeTransform {

    constructor(protected sanitizer: DomSanitizer) {}

    transform(value: string, option?: string): SafeHtml {
        const storedLinkifiers = sessionStorage.getItem('linkifiers');
        if (value == null || storedLinkifiers == null) {
            return value ? value : '';
        }

        let newValue: string = value;
        const linkifiers: Array<Linkifier> = JSON.parse(storedLinkifiers);

        for (const l of linkifiers) {
            const regExp = new RegExp(l.pattern, 'g');
            newValue = newValue.replace(regExp, '<a target="_blank" href="' + l.link + '">' + newValue.match(regExp) + '</a>');
        }

        return this.linkify(this.applyOption(newValue, option));
    }

    private applyOption(value: string, option: string) {
        switch (option) {
            case 'uppercase': return value.toUpperCase();
            default: return value;
        }
    }

    private linkify(value: string): SafeHtml {
        return this.sanitizer.bypassSecurityTrustHtml(value);
    }
}
