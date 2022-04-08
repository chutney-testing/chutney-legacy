import { Injectable } from '@angular/core';

import hljs from 'highlight.js';

@Injectable({
  providedIn: 'root'
})
export class HighLightJService {

  constructor() {
  }

  highlightElement(baseElement: Element, codeSelectorAll: string = 'pre code') {
      const codes = baseElement.querySelectorAll(codeSelectorAll);
      //TODO https://www.npmjs.com/package/ngx-highlightjs codes.forEach(code => hljs.highlightBlock(code));
  }
}
