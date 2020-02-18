import { Component, Input, OnInit, ViewChild, ElementRef, OnChanges, ViewEncapsulation } from '@angular/core';
import AsciidocConverter from '../../../assets/js/asciidoctor-converter';
import { HighLightJService } from '../../shared/highlight/highlight.service';

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
