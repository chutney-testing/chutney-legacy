/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { RouterModule } from '@angular/router';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { TranslateModule } from '@ngx-translate/core';
import { MomentModule } from 'ngx-moment';
import { NgxPaginationModule } from 'ngx-pagination';
import { NgClickOutsideModule } from 'ng-click-outside2';

import { AtomsModule } from '../atoms/atoms.module';
import { SharedModule } from '@shared/shared.module';

import { DeleteConfirmDialogComponent } from './dialog/delete-confirm-dialog/delete-confirm-dialog.component';
import { CollapsiblePanelComponent } from './panel/collapsible-panel/collapsible-panel.component';
import { ErrorPanelComponent } from './panel/error-panel/error-panel.component';
import { PropertyTablePanelComponent } from './panel/property-table-panel/property-table-panel.component';
import { TablePanelComponent } from './panel/table-panel/table-panel.component';
import { EditableLabelComponent } from './forms/editable-label/editable-label.component';
import { InputLineComponent } from './forms/input-line/input-line.component';
import { SearchFieldComponent } from './forms/search-field/search-field.component';
import { ValidationService } from './validation/validation.service';
import { EditableTextAreaComponent } from './forms/editable-text-area/editable-text-area.component';
import { EditableBadgeComponent } from './forms/editable-badge/editable-badge.component';
import { AsciiDoctorComponent } from './asciidoctor/asciidoctor.component';
import { ToastInfoComponent } from './toast/toast-info/toast-info';
import { ImportFileComponent } from './forms/import-file/import-file.component';
import { ImportButtonComponent } from './forms/import-button/import-button.component';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';


@NgModule({
    imports: [
        AtomsModule,
        NgClickOutsideModule,
        CommonModule,
        FormsModule,
        MomentModule,
        NgxPaginationModule,
        RouterModule,
        SharedModule,
        TranslateModule,
        NgbTooltipModule,
        ReactiveFormsModule
    ],
    exports: [
        AsciiDoctorComponent,
        CollapsiblePanelComponent,
        DeleteConfirmDialogComponent,
        EditableBadgeComponent,
        EditableLabelComponent,
        EditableTextAreaComponent,
        ErrorPanelComponent,
        ImportButtonComponent,
        ImportFileComponent,
        InputLineComponent,
        //MenuItemComponent,
        PropertyTablePanelComponent,
        SearchFieldComponent,
        TablePanelComponent,
        ToastInfoComponent,
    ],
    declarations: [
        AsciiDoctorComponent,
        CollapsiblePanelComponent,
        DeleteConfirmDialogComponent,
        EditableBadgeComponent,
        EditableLabelComponent,
        EditableTextAreaComponent,
        ErrorPanelComponent,
        ImportButtonComponent,
        ImportFileComponent,
        InputLineComponent,
       //MenuItemComponent,
        PropertyTablePanelComponent,
        SearchFieldComponent,
        TablePanelComponent,
        ToastInfoComponent,
    ],
    providers: [
        ValidationService,
    ]
})
export class MoleculesModule {
}
