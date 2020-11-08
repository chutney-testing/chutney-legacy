import { InjectionToken } from '@angular/core';
import { en } from './en.translation';

/**
 * Enum of currently supported languages
 */
export enum ChutneyAppLanguage {
  English = 'en',
  French = 'fr',
}

/**
 * Inferring the type of default translation (in this case english)
 * This makes type available for type checking of injected translations
 */
export type Translation = typeof en;

/**
 * Translation token used for dependency injection provider
 */
export const TRANSLATION = new InjectionToken<Translation>('TRANSLATION');
