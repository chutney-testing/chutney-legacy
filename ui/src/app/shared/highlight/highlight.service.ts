import { Injectable } from '@angular/core';

import hljs from 'highlight.js';

@Injectable({
  providedIn: 'root'
})
export class HighLightJService {

  constructor() {
  }

  highlightElement(baseElement: Element, codeSelectorAll: string = 'pre code') {
    var codes = baseElement.querySelectorAll(codeSelectorAll);
    codes.forEach(code => hljs.highlightBlock(code));
  }
}
