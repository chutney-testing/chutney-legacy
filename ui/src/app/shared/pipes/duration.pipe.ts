import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'duration' })
export class DurationPipe implements PipeTransform {
    transform(value: number, mode = 'full'): any {
        const hours = Math.floor(value / 1000 / 60 / 60);
        const minutes = Math.floor((value - hours * 1000 * 60 * 60) / 1000 / 60);
        const seconds = Math.floor((value - hours * 1000 * 60 * 60 - minutes * 1000 * 60) / 1000);
        const milliseconds = value - hours * 1000 * 60 * 60 - minutes * 1000 * 60 - seconds * 1000;

        if (mode === 'full') {
            return this.buildFull(hours, minutes, seconds, milliseconds);
        } else if (mode === 'short') {
            return this.buildShort(hours, minutes, seconds, milliseconds);
        } else {
            throw Error(`InvalidPipeArgument: mode : '${mode}' for pipe DurationPipe`);
        }
    }

    buildFull(hours: number, minutes: number, seconds: number, milliseconds: number) {
        let result = '';

        if (hours > 0) {
            result += hours + 'h ';
        }
        if (minutes > 0) {
            result += minutes + 'm ';
        }
        if (seconds > 0) {
            result += seconds + 's ';
        }
        if (milliseconds > 0) {
            result += milliseconds + 'ms';
        }
        return result.trim();
    }

    buildShort(hours: number, minutes: number, seconds: number, milliseconds: number) {
        if (hours > 0) {
            return hours + ' h';
        } else if (minutes > 0) {
            return minutes + ' m';
        } else if (seconds > 0) {
            return seconds + ' s';
        } else {
            return milliseconds + ' ms';
        }
    }
}
