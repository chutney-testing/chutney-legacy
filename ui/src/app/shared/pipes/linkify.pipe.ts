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
        if (value == null || value === '') {
            // @ts-ignore
            return '';
        }

        if (linkifiers[counter] == null) {
            // @ts-ignore
            return value;
        }

        let result;
        let prevIndex = 0;
        const chunks = [];
        const regex = new RegExp(linkifiers[counter].pattern, 'g');
        const regTmp = new RegExp(linkifiers[counter].pattern);

        while ((result = regex.exec(value)) != null) {
            const substr = value.substring(prevIndex, result.index);
            if (substr !== '')  {
                chunks.splice(prevIndex, 0, this.slice(substr, linkifiers, counter + 1));
            }
            prevIndex = regex.lastIndex;
            const match = value.substring(result.index, regex.lastIndex);
            chunks.push(match.replace(regTmp, '<a target="_blank" href="' + linkifiers[counter].link + '">' + result[0] + '</a>'));
        }

        chunks.push(this.slice(value.substring(prevIndex, value.length), linkifiers, counter + 1));

        return chunks;
    }

    private concat(chunks: string[]): string {
        if (typeof chunks === 'string' || chunks instanceof String) {
            // @ts-ignore
            return chunks;
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
