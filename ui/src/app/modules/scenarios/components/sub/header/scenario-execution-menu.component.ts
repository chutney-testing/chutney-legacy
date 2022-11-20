import { Component, Input, Output, EventEmitter, OnInit, ViewChildren, QueryList, TemplateRef } from '@angular/core';
import { disabledBoolean } from '@shared/tools/bool-utils';

import { TestCase, ScenarioIndex, Authorization } from '@model';
import { ScenarioService, ComponentService, EnvironmentAdminService, JiraPluginService, LoginService } from '@core/services';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { FileSaverService } from 'ngx-filesaver';
import { NgbDropdown } from '@ng-bootstrap/ng-bootstrap';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

@Component({
    selector: 'chutney-scenario-execution-menu',
    templateUrl: './scenario-execution-menu.component.html',
    styleUrls: ['./scenario-execution-menu.component.scss']
})
export class ScenarioExecutionMenuComponent implements OnInit {

    @Input() testCaseId: string;
    @Input() canExecute = true;

    @Output() executeEvent = new EventEmitter<string>();

    isComposed = TestCase.isComposed;

    environments: Array<string>;
    testCaseMetadata: ScenarioIndex;

    @ViewChildren(NgbDropdown)
    private executeDropDown: QueryList<NgbDropdown>;

    Authorization = Authorization;
    modalRef: BsModalRef;

    constructor(private componentService: ComponentService,
                private environmentAdminService: EnvironmentAdminService,
                private fileSaverService: FileSaverService,
                private jiraLinkService: JiraPluginService,
                private router: Router,
                private scenarioService: ScenarioService,
                private loginService: LoginService,
                private modalService: BsModalService
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
        if (this.environments.length === 1) {
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

    openModal(template: TemplateRef<any>) {
        this.modalRef = this.modalService.show(template, {class: 'modal-sm'});
        document.getElementById('no-btn').focus();
    }

    confirm(): void {
        this.modalRef.hide();
        this.deleteScenario(this.testCaseId);
    }

    decline(): void {
        this.modalRef.hide();
    }



    private removeJiraLink(id: string) {
        this.jiraLinkService.removeForScenario(id).subscribe(
            () => {},
            (error) => { console.log(error); }
        );
    }
}
