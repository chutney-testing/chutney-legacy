import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'searchTextPipe'
})
export class SearchTextPipe implements PipeTransform {

    transform(input: any, filtertext: string, args: any[]) {

        return input.filter((item) => {
            return filtertext === undefined
                || filtertext === ''
                || args.map(a => item[a] !== undefined && this.normalize(item[a]).indexOf(this.normalize(filtertext)) !== -1)
                        .reduce( (p, c) => p || c);
        });
    }

    private normalize(value) {
        return value.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    }
}
