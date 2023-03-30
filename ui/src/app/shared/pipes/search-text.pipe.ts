import { Pipe, PipeTransform } from '@angular/core';
import { filterOnTextContent } from '@shared/tools';

@Pipe({
    name: 'searchTextPipe'
})
export class SearchTextPipe implements PipeTransform {

    transform(input: any, filtertext: string, args: any[] = null) {
        return filterOnTextContent(input, filtertext, args);
    }

}
