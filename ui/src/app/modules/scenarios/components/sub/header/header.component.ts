import { Component, Input, Output, EventEmitter, OnInit, ViewChildren, QueryList } from '@angular/core';
import { disabledBoolean } from '@shared/tools/bool-utils';

import { TestCase, ScenarioIndex, Authorization } from '@model';
import { ScenarioService, ComponentService, EnvironmentAdminService, JiraPluginService, LoginService } from '@core/services';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { FileSaverService } from 'ngx-filesaver';
import { NgbDropdown } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'chutney-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

    @Input() testCaseId: string;
    @Input() canExecute = true;

    @Output() executeEvent = new EventEmitter<string>();

    isComposed = TestCase.isComposed;

    environments: Array<string>;
    testCaseMetadata: ScenarioIndex;

    @ViewChildren(NgbDropdown)
    private executeDropDown: QueryList<NgbDropdown>;

    Authorization = Authorization;

    constructor(private componentService: ComponentService,
                private environmentAdminService: EnvironmentAdminService,
                private fileSaverService: FileSaverService,
                private jiraLinkService: JiraPluginService,
                private router: Router,
                private scenarioService: ScenarioService,
                private loginService: LoginService
    ) {
    }

    ngOnInit(): void {
        this.scenarioService.findScenarioMetadata(this.testCaseId).subscribe(
            (res) => this.testCaseMetadata = res
        );
        if (this.loginService.hasAuthorization(Authorization.SCENARIO_EXECUTE)) {
            this.environmentAdminService.listEnvironmentsNames().subscribe(
                (res) => this.environments = res
            );
        }
    }

    executeScenario(envName: string) {
        this.executeEvent.emit(envName);
    }

    executeScenarioOnToggle() {
        if (this.environments.length == 1) {
            this.executeDropDown.first.close();
            this.executeScenario(this.environments[0]);
        }
    }

    deleteScenario(id: string) {
        let deleteObs: Observable<any>;
        if (TestCase.isComposed(this.testCaseId)) {
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
        if (TestCase.isComposed(this.testCaseId)) {
            this.router.navigateByUrl('/scenario/' + this.testCaseId + '/component-edition?duplicate=true');
        } else {
            this.router.navigateByUrl('/scenario/' + this.testCaseId + '/raw-edition?duplicate=true');
        }
    }

    exportScenario() {
        const fileName = `${this.testCaseId}-${this.testCaseMetadata.title}.chutney.hjson`;
        this.scenarioService.findRawTestCase(this.testCaseId).subscribe((testCase: TestCase) => {
            this.fileSaverService.saveText(testCase.content, fileName);
        });
    }

    isNotEditable() {
        return disabledBoolean(this.isNotLocalSource());
    }

    isNotComposed() {
        return !TestCase.isComposed(this.testCaseId);
    }

    private isNotLocalSource(): boolean {
        const source = this.testCaseMetadata.repositorySource;
        return !((source == 'local') || (source == 'ComposableTestCase'));
    }

    private removeJiraLink(id: string) {
        this.jiraLinkService.removeForScenario(id).subscribe(
            () => {},
            (error) => { console.log(error); }
        );
    }
}
