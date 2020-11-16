import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExecutionBadgeComponent } from './components/execution-badge/execution-badge.component';
import { ExecutionIconComponent } from './components/execution-icon/execution-icon.component';
import { MatIconModule } from '@angular/material/icon';

@NgModule({
  imports: [CommonModule, MatIconModule],
  declarations: [ExecutionBadgeComponent, ExecutionIconComponent],
  exports: [ExecutionBadgeComponent, ExecutionIconComponent],
})
export class UiCommonsModule {}
