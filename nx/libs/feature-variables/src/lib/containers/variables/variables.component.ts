import { Component } from '@angular/core';

import {
  chutneyAnimations,
} from '@chutney/utils';

@Component({
  selector: 'chutney-variables',
  templateUrl: './variables.component.html',
  styleUrls: ['./variables.component.scss'],
  animations: [chutneyAnimations],
})
export class VariablesComponent {
  breadcrumbs: any = [
    { title: 'Home', link: ['/'] },
    { title: 'Variables', link: ['/variables'] },
  ];

  constructor() {}
}
