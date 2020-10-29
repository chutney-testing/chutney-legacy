import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatPassToggleVisibilityComponent } from './components/mat-pass-toggle-visibility/mat-pass-toggle-visibility.component';
import { MatIconModule } from '@angular/material/icon';
import { MatRippleModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';



@NgModule({
  declarations: [MatPassToggleVisibilityComponent],
  exports: [MatPassToggleVisibilityComponent],
  imports: [
    CommonModule,
    MatIconModule,
    MatRippleModule,
    MatButtonModule
  ]
})
export class MatPasswordModule { }
