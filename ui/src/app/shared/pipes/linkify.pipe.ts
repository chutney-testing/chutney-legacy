import { Pipe, PipeTransform } from '@angular/core';
import { Linkifier } from '@core/services';
import { SafeHtml } from '@angular/platform-browser';

@Pipe({name: 'linkify'})
export class LinkifyPipe implements PipeTransform {

    transform(value: string, option?: string): SafeHtml {
        let newValue: string = value;
        const storedLinkifiers = sessionStorage.getItem('linkifiers');
        const linkifiers: Array<Linkifier> = storedLinkifiers ? JSON.parse(storedLinkifiers) : [];

        for (const l of linkifiers) {
            const regExp = new RegExp(l.pattern, 'g');
            newValue = newValue.replace(regExp, '<a target="_blank" href="' + l.link + '">' + newValue.match(regExp) + '</a>');
        }

        return this.linkify(this.applyOption(newValue, option));
    }

    private linkify(value: string): SafeHtml {
        const doc = new DOMParser().parseFromString(value.toString(), 'text/html');
        return doc.documentElement.innerHTML;
    }

    private applyOption(value: string, option: string) {
        switch (option) {
            case 'uppercase': return value.toUpperCase();
            default: return value;
        }
    }
}
