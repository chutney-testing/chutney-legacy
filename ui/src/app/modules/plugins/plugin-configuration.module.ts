import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { PluginConfigurationRoute } from './plugin-configuration.routes';

import { MoleculesModule } from '../../molecules/molecules.module';
import { PluginConfigurationComponent } from './components/plugin-configuration.component';
import { ReactiveFormsModule } from '@angular/forms';
import { JiraComponent } from '@modules/plugins/components/jira/jira.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(PluginConfigurationRoute),
    FormsModule,
    TranslateModule,
    MoleculesModule,
    ReactiveFormsModule
  ],
  declarations: [
      JiraComponent,
      PluginConfigurationComponent
  ],
})
export class PluginConfigurationModule {
}
