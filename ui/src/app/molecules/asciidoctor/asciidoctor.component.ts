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

import { Component, Input, OnInit, ViewChild, ElementRef, OnChanges, ViewEncapsulation } from '@angular/core';
import AsciidocConverter from '../../../assets/js/asciidoctor-converter';
import { HighLightJService } from '@shared/highlight/highlight.service';

@Component({
  selector: 'chutney-asciidoctor',
  templateUrl: './asciidoctor.component.html',
  styleUrls: ['./asciidoctor.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class AsciiDoctorComponent implements OnInit, OnChanges {

  @Input() doc: string;
  @Input() tocLeft: boolean = false;
  @Input() tocScrollHack: boolean = false;
  @Input() highlight: boolean = true;

  @ViewChild('asciidocHTML') asciidocHTML: ElementRef;

  private tocLeftStyleClass: string = 'with-toc';
  private converter: AsciidocConverter = new AsciidocConverter();

  constructor(private highLightJService: HighLightJService) {
  }

  ngOnInit() {
  }

  ngOnChanges() {
    if (this.doc && this.doc.length > 0) {
      const el = this.asciidocHTML.nativeElement;

      el.innerHTML = this.converter.convert(this.doc);
      if (this.tocLeft) {
        this.converter.styleEmbeddedDocWithLeftToc(el, this.tocLeftStyleClass);
      }

      if (this.highlight) {
        this.highLightJService.highlightElement(el);
      }

      if (this.tocScrollHack) {
        el.onclick = (event) => {
          if (event.target && this.converter.isElementFromToc(el, event.target)) {
            if (event.target.attributes['href']) {
              el.querySelector(event.target.attributes['href'].nodeValue)
                .scrollIntoView({behavior: 'instant', block: 'center', inline: 'center'});
              event.preventDefault();
            }
          }
        };
      }
    }
  }
}
