import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { PluginConfigurationRoute } from './plugin-configuration.routes';

import { MoleculesModule } from '../../molecules/molecules.module';
import { PluginConfigurationComponent } from './components/plugin-configuration.component';
import { GitBackupComponent } from '@modules/plugins/components/git-backup/git-backup.component';
import { JiraComponent } from '@modules/plugins/components/jira/jira.component';
import { LinkifierComponent } from '@modules/plugins/components/linkifier/linkifier.component';
import { SharedModule } from '@shared/shared.module';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        MoleculesModule,
        ReactiveFormsModule,
        RouterModule.forChild(PluginConfigurationRoute),
        SharedModule,
        TranslateModule,
    ],
    declarations: [
        GitBackupComponent,
        JiraComponent,
        LinkifierComponent,
        PluginConfigurationComponent
    ],
})
export class PluginConfigurationModule {
}
