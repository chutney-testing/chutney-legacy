import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'stringify'
})
export class StringifyPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    if (value instanceof Object) {
        return JSON.stringify(value);
    } else {
        return value;
    }
  }
}
