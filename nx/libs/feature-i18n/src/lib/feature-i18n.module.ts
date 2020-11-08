import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

/**
 * Should contain imports and providers for stuff that is needed for
 * every internationalized side module as it is imported in them
 */
@NgModule({
  imports: [CommonModule],
  exports: [CommonModule],
  declarations: [],
})
export class FeatureI18nModule {}
