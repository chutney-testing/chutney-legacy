import { Component, Input, OnInit, ViewEncapsulation } from '@angular/core';
import { Router } from '@angular/router';
import { chutneyAnimations } from '@chutney/utils';

@Component({
  selector: 'chutney-page-header',
  host: {
    class: 'chutney-page-header',
  },
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss'],
  animations   : chutneyAnimations,
  encapsulation: ViewEncapsulation.None,
})
export class PageHeaderComponent implements OnInit {

  @Input() title = '';
  @Input() subtitle = '';
  @Input() showBreadCrumb = true;

  constructor(private router: Router) {
  }

  ngOnInit() {
  }

  genBreadcrumb() {
  }

}
