import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

import { DocumentationRoute } from './documentation.routes';

import { AtomsModule } from '../../atoms/atoms.module';
import { DocumentationComponent } from './documentation.component';
import { MoleculesModule } from '../../molecules/molecules.module';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(DocumentationRoute),
    TranslateModule,
    AtomsModule,
    MoleculesModule,
    SharedModule
  ],
  declarations: [DocumentationComponent]
})
export class DocumentationModule {
}
