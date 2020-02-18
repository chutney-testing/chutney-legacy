import { Injectable } from '@angular/core';

import * as hjson from 'hjson';

@Injectable()
export class HjsonParserService {

  parse(content: string): string {
    return JSON.stringify(hjson.parse(content));
  }
}
