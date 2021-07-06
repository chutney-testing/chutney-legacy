import { Component, Input} from '@angular/core';
import { ComponentTask, Authorization } from '@core/model';


@Component({
    selector: 'chutney-child-panel-component',
    templateUrl: './child-panel.component.html',
    styleUrls: ['./child-panel.component.scss']
})
export class ChildPanelComponent {

    @Input() parents: any;
    @Input() componentTask: ComponentTask;
    @Input() show = false;

    Authorization = Authorization;

    constructor(
    ) {
    }

}
