import { Component, OnInit, ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'chutney-doc-page',
  templateUrl: './documentation.component.html',
  styleUrls: ['./documentation.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class DocumentationComponent implements OnInit {

  documentation: string;

  constructor() { }

  ngOnInit() {
    fetch('/assets/doc/user_manual.adoc')
    .then(response => response.text())
    .then((data) => {
      this.documentation = data;
    });
  }

}
