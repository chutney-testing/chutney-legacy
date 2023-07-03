import { Injectable } from '@angular/core';


@Injectable()
export class ValidationService {

    private urlRegex = new RegExp('^[a-z][a-z0-9\+-\.]*:\/\/[^:]+(:[0-9]+)?.*$');
    private spelRegex = new RegExp('\\$\\{([^}]+)\\}');
    private environmentNameRegex = new RegExp('^[a-zA-Z0-9_-]{3,20}$');

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
    isValidEnvironmentName(text: string): boolean {
        return text !== null && this.environmentNameRegex.test(text);
    }

    isValidPattern(text: string) {
        try {
            new RegExp(text);
        } catch {
            return false;
        }
        return true;
    }
}
