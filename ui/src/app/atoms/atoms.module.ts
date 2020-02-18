import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ButtonComponent } from './buttons/button.component';
import { ToggleButtonComponent } from './buttons/toggle-button.component';
import { LinkComponent } from './buttons/link.component';
import { InputComponent } from './forms/input/input.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule
    ],
    exports: [
        ButtonComponent,
        ToggleButtonComponent,
        LinkComponent,
        InputComponent
    ],
    declarations: [
        ButtonComponent,
        ToggleButtonComponent,
        LinkComponent,
        InputComponent
    ]
})
export class AtomsModule {
}
