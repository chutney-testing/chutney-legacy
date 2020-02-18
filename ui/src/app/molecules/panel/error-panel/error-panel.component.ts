import { Component, Input} from '@angular/core';

@Component({
  selector: 'chutney-error-panel',
  templateUrl: './error-panel.component.html'
})
export class ErrorPanelComponent {

  @Input() errorMessage: string;
}
