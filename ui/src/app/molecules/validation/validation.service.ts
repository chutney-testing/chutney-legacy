import { Injectable } from '@angular/core';


@Injectable()
export class ValidationService {

    private urlRegex = new RegExp('^[a-z]+:\/\/[^:]+:[0-9]+.*$');
    private environmentNameRegex = new RegExp('^[A-Z0-9_-]{3,20}$');

    constructor() { }

    isNotEmpty(text: string): boolean {
        return text !== null && text !== '';
    }

    isValidUrl(text: string): boolean {
        return this.urlRegex.test(text);
    }

    isValidEnvironmentName(text: string): boolean {
      return this.environmentNameRegex.test(text);
    }
}
