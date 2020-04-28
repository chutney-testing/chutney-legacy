import { Pipe, PipeTransform } from '@angular/core';
import { filterOnTextContent } from '@shared/tools';

@Pipe({
    name: 'searchTextPipe'
})
export class SearchTextPipe implements PipeTransform {

    transform(input: any, filtertext: string, args: any[]) {
        return filterOnTextContent(input, filtertext, args);
    }

}
