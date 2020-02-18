import { ValidatorFn, AbstractControl } from '@angular/forms';

export function durationValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: boolean } | null => {
        if (control.value && /(\d+(?:[.,]\d+)?)\s+(?:ms|s|sec|m|min|h|hour|hours|hours\(s\)|d|day|days|day\(s\))/.test(control.value)) {
            return null;
        }
        return { 'duration': false};
    };
}
