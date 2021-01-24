import { Component, Input, ViewEncapsulation } from '@angular/core';

type Type = 'text' | 'password';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'mat-pass-toggle-visibility',
  templateUrl: './mat-pass-toggle-visibility.component.html',
  styleUrls: ['./mat-pass-toggle-visibility.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class MatPassToggleVisibilityComponent {
  @Input()
  isVisible: boolean;

  _type: Type = 'text';

  get type() {
    return this.isVisible ? 'text' : 'password';
  }
}
