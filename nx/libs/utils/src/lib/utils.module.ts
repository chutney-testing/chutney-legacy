import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DurationPipe } from './pipes/duration/duration.pipe';

@NgModule({
  imports: [CommonModule],
  declarations: [DurationPipe],
  exports: [DurationPipe],
})
export class UtilsModule {}
