import { Pipe, PipeTransform } from '@angular/core';

const SECONDS = 1000;
const MINUTE = 60 * SECONDS;
const HOUR: number = MINUTE * 60;
const DAY: number = HOUR * 24;

@Pipe({
  name: 'duration',
})
export class DurationPipe implements PipeTransform {
  transform(value: any, args?: any): any {
    if (isNaN(value)) {
      return '-';
    }
    let duration = '';
    let valueLeft: number = value;
    if (value > DAY) {
      duration += Math.floor(value / DAY) + ' d ';
      valueLeft = Math.floor(value % DAY);
    }
    if (value > HOUR && valueLeft > HOUR) {
      duration += Math.floor(valueLeft / HOUR) + ' h ';
      valueLeft = Math.floor(valueLeft % HOUR);
    }
    if (value > MINUTE && valueLeft > MINUTE) {
      duration += Math.floor(valueLeft / MINUTE) + ' min ';
      valueLeft = Math.floor(valueLeft % MINUTE);
    }
    if (value > SECONDS && valueLeft > SECONDS) {
      duration += Math.floor(valueLeft / SECONDS) + ' s ';
      valueLeft = Math.floor(valueLeft % SECONDS);
    }
    if (valueLeft >= 0) {
      duration += valueLeft + ' ms';
    }

    return duration;
  }
}
