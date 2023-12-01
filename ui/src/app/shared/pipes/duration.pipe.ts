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
