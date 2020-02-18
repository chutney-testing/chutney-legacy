import { Pipe, PipeTransform } from '@angular/core';

/**
 * Usage :
 * For example an object Scenario with field title
 * To order by title in desc order :
 * *ngFor="let scenario of (scenarios |  sortByField : 'title' : true)">
 */
@Pipe({ name: 'sortByField' })
export class SortByFieldPipe implements PipeTransform {

  transform(array: any[], field: string, reverse: boolean): any[] {
    if (field != null && field !== '') {
      array.sort((a: any, b: any) => {
        const fieldA = this.resolve(field, a);
        const fieldB = this.resolve(field, b);

        if (fieldA == null && fieldB == null) {
          return 0;
        } else if (fieldB == null) {
          return reverse ? -1 : 1;
        } else if (fieldA == null) {
          return reverse ? 1 : -1;
        }
        if (fieldA < fieldB) {
          return reverse ? 1 : -1;
        } else if (fieldA > fieldB) {
          return reverse ? -1 : 1;
        } else {
          return 0;
        }
      });
    }
    return array;
  }

  // Usage :
  // resolve("style.width", document.body)
  // resolve("part.0.size", someObject)
  // returns null when intermediate properties are not defined
  resolve(path, obj) {
    return path.split('.').reduce(function (prev, curr) {
      return prev ? prev[curr] : null;
    }, obj || self);
  }
}
