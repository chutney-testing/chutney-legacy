import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { ConfigurationRoute } from './configuration.routes';

import { MoleculesModule } from '../../molecules/molecules.module';
import { ConfigurationComponent } from './components/configuration.component';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(ConfigurationRoute),
    FormsModule,
    TranslateModule,
    MoleculesModule,
    ReactiveFormsModule
  ],
  declarations: [ConfigurationComponent],
})
export class ConfigurationModule {
}
