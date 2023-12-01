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
import { SafeHtml, SafeValue } from '@angular/platform-browser';

@Pipe({
    name: 'thumbnail'
})
export class ThumbnailPipe implements PipeTransform {

    constructor() {}

    public transform(value: SafeValue): SafeHtml {
        const doc = new DOMParser().parseFromString(value.toString(), 'text/html');
        const imgElements = doc.getElementsByTagName('img');
        for (let i = 0; i < imgElements.length; i++) {
            const imgElement = imgElements.item(i);
            imgElement.classList.add('img-thumbnail');
            imgElement.insertAdjacentHTML('beforebegin', '<a href="' + imgElement.src + '" target="_blank">' + imgElement.outerHTML + '</a>');
            imgElement.remove();
        }
        return doc.documentElement.innerHTML;
    }
}
