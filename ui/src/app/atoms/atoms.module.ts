import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ButtonComponent } from './buttons/button.component';
import { LinkComponent } from './buttons/link.component';
import { InputComponent } from './forms/input/input.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule
    ],
    exports: [
        ButtonComponent,
        LinkComponent,
        InputComponent
    ],
    declarations: [
        ButtonComponent,
        LinkComponent,
        InputComponent
    ]
})
export class AtomsModule {
}
