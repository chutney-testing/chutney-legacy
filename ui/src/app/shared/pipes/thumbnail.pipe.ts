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
            imgElement.classList.add('thumbnail');
            imgElement.insertAdjacentHTML('beforebegin', '<a href="' + imgElement.src + '" target="_blank">' + imgElement.outerHTML + '</a>');
            imgElement.remove();
        }
        return doc.documentElement.innerHTML;
    }
}
