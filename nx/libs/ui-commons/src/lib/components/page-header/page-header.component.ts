import {
  Component,
  Directive,
  Input,
  OnInit,
  ViewEncapsulation,
} from '@angular/core';
import { Router } from '@angular/router';
import { chutneyAnimations } from '@chutney/utils';

/**
 */
@Directive({
  selector: `chutney-page-header-extra, [chutney-page-header-extra], [chutneyPageHeaderExtra]`,
  host: {
    class: 'chutney-page-header-extra',
  },
})
export class PageHeaderExtra {}

@Component({
  selector: 'chutney-page-header',
  host: {
    class: 'chutney-page-header',
  },
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss'],
  animations: chutneyAnimations,
  encapsulation: ViewEncapsulation.None,
})
export class PageHeaderComponent implements OnInit {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() showBreadCrumb = true;
  @Input() breadcrumbs: any;

  constructor(private router: Router) {}

  ngOnInit() {}
}
