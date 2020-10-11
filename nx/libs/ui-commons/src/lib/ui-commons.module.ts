import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExecutionBadgeComponent } from './components/execution-badge/execution-badge.component';

@NgModule({
  imports: [CommonModule],
  declarations: [ExecutionBadgeComponent],
  exports: [ExecutionBadgeComponent],
})
export class UiCommonsModule {}
