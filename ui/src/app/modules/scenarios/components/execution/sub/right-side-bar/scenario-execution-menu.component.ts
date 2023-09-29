import {
    Component,
    OnInit,
    QueryList,
    TemplateRef,
    ViewChild,
    ViewChildren
} from '@angular/core';

import { Authorization, ScenarioIndex, TestCase } from '@model';
import {
    JiraPluginService,
    LoginService,
    ScenarioService
} from '@core/services';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatestWith, Observable, of, switchMap, tap } from 'rxjs';
import { FileSaverService } from 'ngx-filesaver';
import { NgbDropdown } from '@ng-bootstrap/ng-bootstrap';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { EventManagerService } from '@shared';
import { MenuItem } from '@shared/components/layout/menuItem';
import { EnvironmentService } from '@core/services/environment.service';

@Component({
    selector: 'chutney-scenario-execution-menu',
    templateUrl: './scenario-execution-menu.component.html',
    styleUrls: ['./scenario-execution-menu.component.scss']
})
export class ScenarioExecutionMenuComponent implements OnInit {

    testCaseId: string;
    canExecute = true;

    environments: Array<string>;
    testCaseMetadata: ScenarioIndex;

    @ViewChildren(NgbDropdown)
    private executeDropDown: QueryList<NgbDropdown>;

    @ViewChild('delete_modal') deleteModal: TemplateRef<any>;

    Authorization = Authorization;
    modalRef: BsModalRef;
    rightMenuItems: MenuItem[];

    constructor(private environmentService: EnvironmentService,
                private fileSaverService: FileSaverService,
                private jiraLinkService: JiraPluginService,
                private router: Router,
                private scenarioService: ScenarioService,
                private loginService: LoginService,
                private modalService: BsModalService,
                private route: ActivatedRoute,
                private eventManagerService: EventManagerService) {
    }


    ngOnInit(): void {
        this.route.params
            .pipe(
                tap(params => this.testCaseId = params['id']),
                switchMap(() => this.scenarioService.findScenarioMetadata(this.testCaseId)),
                combineLatestWith(this.getEnvironments())
            )
            .subscribe(([scenarioMetadata, environments]) => {
                this.testCaseMetadata = scenarioMetadata;
                this.environments = environments;
                this.initRightMenu();
            });


    }

    executeScenario(envName: string) {
        this.eventManagerService.broadcast({name: 'execute', env: envName});
    }

    deleteScenario(id: string) {
        let delete$ = this.scenarioService.delete(id);

        delete$.subscribe(() => {
            this.removeJiraLink(id);
            this.router.navigateByUrl('/scenario')
                .then(null);
        });
    }

    duplicateScenario() {
        this.router.navigateByUrl('/scenario/' + this.testCaseId + '/raw-edition' + '?duplicate=true');
    }

    exportScenario() {
        const fileName = `${this.testCaseId}-${this.testCaseMetadata.title}.chutney.hjson`;
        this.scenarioService.findRawTestCase(this.testCaseId).subscribe((testCase: TestCase) => {
            this.fileSaverService.saveText(testCase.content, fileName);
        });
    }

    openModal() {
        this.modalRef = this.modalService.show(this.deleteModal, {class: 'modal-sm'});
    }

    confirm(): void {
        this.modalRef.hide();
        this.deleteScenario(this.testCaseId);
    }

    decline(): void {
        this.modalRef.hide();
    }


    private removeJiraLink(id: string) {
        this.jiraLinkService.removeForScenario(id).subscribe({
            error: (error) => {
                console.log(error);
            }
        });
    }

    private initRightMenu() {
        const rightMenuItems: any [] = [
            {
                label: 'global.actions.execute',
                click: this.executeScenario.bind(this),
                iconClass: 'fa fa-play',
                authorizations: [Authorization.SCENARIO_EXECUTE],
                options: this.environments.map(env => {
                    return {id: env, label: env};
                })
            },
            {
                label: 'global.actions.edit',
                link: '/scenario/' + this.testCaseId + '/raw-edition',
                iconClass: 'fa fa-pencil-alt',
                authorizations: [Authorization.SCENARIO_WRITE]
            },
            {
                label: 'global.actions.delete',
                click: this.openModal.bind(this),
                iconClass: 'fa fa-trash',
                authorizations: [Authorization.SCENARIO_WRITE]
            },
            {
                label: 'global.actions.clone',
                click: this.duplicateScenario.bind(this),
                iconClass: 'fa fa-clone',
                authorizations: [Authorization.SCENARIO_WRITE]
            },
            {
                label: 'global.actions.export',
                click: this.exportScenario.bind(this),
                iconClass: 'fa fa-file-code'
            }
        ];
        
        this.rightMenuItems = rightMenuItems;
    }

    private getEnvironments(): Observable<Array<string>> {
        if (this.loginService.hasAuthorization(Authorization.SCENARIO_EXECUTE)) {
            return this.environmentService.names();
        }
        return of([])
    }

}
