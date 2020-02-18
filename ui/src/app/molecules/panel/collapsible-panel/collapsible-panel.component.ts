import { Component, Input} from '@angular/core';

@Component({
  selector: 'chutney-collapsible-panel',
  templateUrl: './collapsible-panel.component.html',
  styleUrls: ['./collapsible-panel.component.scss']
})
export class CollapsiblePanelComponent {

    collapsed = true;
    @Input() title: string;
}
