import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExecutionBadgeComponent } from './components/execution-badge/execution-badge.component';
import { ExecutionIconComponent } from './components/execution-icon/execution-icon.component';
import { MatIconModule } from '@angular/material/icon';
import { PageHeaderComponent } from './components/page-header/page-header.component';
import { UiMaterialModule } from '@chutney/ui-material';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [CommonModule, MatIconModule, UiMaterialModule, RouterModule],
  declarations: [
    ExecutionBadgeComponent,
    ExecutionIconComponent,
    PageHeaderComponent,
  ],
  exports: [
    ExecutionBadgeComponent,
    ExecutionIconComponent,
    PageHeaderComponent,
  ],
  schemas: [NO_ERRORS_SCHEMA],
})
export class UiCommonsModule {}
