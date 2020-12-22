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

        const linkifiers: Array<Linkifier> = JSON.parse(storedLinkifiers);

        const chunks = this.slice(value, linkifiers, 0);
        const newValue = this.concat(chunks);

        return this.linkify(this.applyOption(newValue, option));
    }

    private slice(value: string, linkifiers: Array<Linkifier>, counter: number): string[] {
        if (linkifiers[counter] == null) {
            return [value];
        }

        let result;
        let prevIndex = 0;
        const chunks = [];
        const regex = new RegExp(linkifiers[counter].pattern, 'g');
        const regTmp = new RegExp(linkifiers[counter].pattern);

        while ((result = regex.exec(value)) !== null) {
            chunks.splice(prevIndex, 0, this.slice(value.substr(prevIndex, result.index), linkifiers, counter + 1));
            prevIndex = regex.lastIndex;
            const match = value.substr(result.index, regex.lastIndex - result.index);
            chunks.push(match.replace(regTmp, '<a target="_blank" href="' + linkifiers[counter].link + '">' + result[0] + '</a>'));
        }

        chunks.push(this.slice(value.substr(prevIndex, value.length), linkifiers, counter + 1));

        return chunks;
    }

    private concat(chunks: string[]): string {
        if (chunks.length === 1) {
            return chunks[0];
        }

        let slice = '';
        for (const chunk of chunks) {
            // @ts-ignore
            slice += this.concat(chunk);
        }

        return slice;
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
