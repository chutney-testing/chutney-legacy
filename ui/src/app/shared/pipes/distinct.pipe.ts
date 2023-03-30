import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'distinct'
})
export class DistinctPipe implements PipeTransform {

    transform(values: any [], property: string = null): any [] {
        return values.filter((value, index, self) =>
            index === self.findIndex(other =>other[property] === value[property]));
    }

}
