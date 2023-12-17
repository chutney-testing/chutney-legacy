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

import { Injectable } from '@angular/core';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';


@Injectable()
export class ValidationService {

    private urlRegex = new RegExp('^[a-z][a-z0-9\+-\.]*:\/\/[^:]+(:[0-9]+)?.*$');
    private spelRegex = new RegExp('\\$\\{([^}]+)\\}');
    private nameRegex = new RegExp('^[a-zA-Z0-9_-]{3,20}$');

    constructor() { }

    isNotEmpty(text: string): boolean {
        return text != null && text.trim() !== '';
    }

    isValidUrl(text: string): boolean {
        return this.urlRegex.test(text);
    }

    isValidSpel(text: string): boolean {
        return this.spelRegex.test(text);
    }

    isValidUrlOrSpel(text: string): boolean {
        return this.isValidUrl(text) || this.isValidSpel(text);
    }
    isValidName(text: string): boolean {
        return text !== null && this.nameRegex.test(text);
    }

    isValidPattern(text: string) {
        try {
            new RegExp(text);
        } catch {
            return false;
        }
        return true;
    }

    asValidatorFn(fn: (value: any) => boolean, errorName: string) : ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const valid = fn(control.value);
            return valid ? null : { [errorName]: true };
        };
    }
}
