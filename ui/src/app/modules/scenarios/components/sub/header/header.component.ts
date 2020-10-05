import { Component, Input, Output, EventEmitter, OnInit, ViewChildren, QueryList } from '@angular/core';
import { disabledBoolean } from '@shared/tools/bool-utils';

import { TestCase, EnvironmentMetadata } from '@model';
import { ScenarioService, ComponentService, EnvironmentAdminService } from '@core/services';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { FileSaverService } from 'ngx-filesaver';
import { NgbDropdown } from '@ng-bootstrap/ng-bootstrap';
import { JiraPluginService } from '@core/services/jira-plugin.service';

@Component({
    selector: 'chutney-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {


    @Input() testCase: TestCase;
    @Input() canExecute = true;

    @Output() executeEvent = new EventEmitter<string>();

    isComposed = TestCase.isComposed;

    environments: EnvironmentMetadata[];

    @ViewChildren(NgbDropdown)
    private executeDropDown: QueryList<NgbDropdown>;

    constructor(private componentService: ComponentService,
                private environmentAdminService: EnvironmentAdminService,
                private fileSaverService: FileSaverService,
                private jiraLinkService: JiraPluginService,
                private router: Router,
                private scenarioService: ScenarioService,
    ) {
    }

    ngOnInit(): void {
        this.environmentAdminService.listEnvironments().subscribe(
            (res) => this.environments = res
        );
    }

    executeScenario(envName: string) {
        this.executeEvent.emit(envName);
    }

    executeScenarioOnToggle() {
        if (this.environments.length == 1) {
            this.executeDropDown.first.close();
            this.executeScenario(this.environments[0].name);
        }
    }

    deleteScenario(id: string) {
        let deleteObs: Observable<any>;
        if (TestCase.isComposed(this.testCase.id)) {
            deleteObs = this.componentService.deleteComponentTestCase(id);
        } else {
            deleteObs = this.scenarioService.delete(id);
        }
        deleteObs.subscribe(() => {
            this.removeJiraLink(id);
            this.router.navigateByUrl('/scenario')
                .then(null);
        });
    }

    duplicateScenario() {
        if (TestCase.isComposed(this.testCase.id)) {
            this.router.navigateByUrl('/scenario/' + this.testCase.id + '/component-edition?duplicate=true');
        } else {
            this.router.navigateByUrl('/scenario/' + this.testCase.id + '/raw-edition?duplicate=true');
        }
    }

    exportScenario() {
        const fileName = `${this.testCase.id}-${this.testCase.title}.chutney.hjson`;
        this.fileSaverService.saveText(this.testCase.content, fileName);
    }

    isNotEditable() {
        return disabledBoolean(this.isNotLocalSource());
    }

    isNotComposed() {
        return !TestCase.isComposed(this.testCase.id);
    }

    private isNotLocalSource(): boolean {
        const source = this.testCase.repositorySource;
        return source == null || source !== 'local';
    }

    private removeJiraLink(id: string) {
        this.jiraLinkService.removeForScenario(id).subscribe(
            () => {},
            (error) => { console.log(error); }
        );
    }
}
