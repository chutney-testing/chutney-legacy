import { Component, Input } from '@angular/core';

@Component({
    selector: 'chutney-plugin-section',
    templateUrl: './plugin-section.component.html',
    styleUrls: ['./plugin-section.component.scss']
})
export class PluginSectionComponent {

    @Input() definition: any;

}
