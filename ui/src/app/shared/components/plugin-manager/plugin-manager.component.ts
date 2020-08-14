import { Component, Input } from '@angular/core';
import { PluginManagerService } from '@core/services';

@Component({
    selector: 'chutney-plugin-manager',
    templateUrl: './plugin-manager.component.html',
    styleUrls: ['./plugin-manager.component.scss']
})
export class PluginManagerComponent {
    @Input() page: string;
    @Input() section: string;

    plugins: any;

    constructor(private pluginManagerService: PluginManagerService) {
    }

    ngOnInit(): void {
        this.pluginManagerService.getDefinitions(this.page, this.section).subscribe(
            (res) => {
                this.plugins = res;
            },
            (error) => console.log(error)
        );
    }
}
