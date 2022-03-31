import { Injectable } from '@angular/core';

import { parse } from 'yaml'

@Injectable()
export class YamlParserService {

    parse(content: string): string {
        return parse(content);
    }
}
