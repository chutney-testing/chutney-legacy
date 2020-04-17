import { FunctionalStep } from '@model';

import { escapeRegExp } from './regexp-utils';
import { distinct } from './array-utils';
import { isNullOrBlankString, escapeHtml } from './string-utils';


export function getStepParamRegExp(stepParamName: string, no_suffix: string = ''): RegExp {
  if (stepParamName != null) {
    return new RegExp('\\*\\*' + escapeRegExp(stepParamName) + '\\*\\*'+(no_suffix.length > 0 ? '(?!'+escapeRegExp(no_suffix)+')' : ''), 'ig');
  }
}

export function exampleParamsExistStepParams(exampleParamsSerialized: any, stepParams: Array<string>): boolean {
  let exists = false;
  const examplesParamsKeys: Array<string> = Object.keys(exampleParamsSerialized);
  stepParams.forEach(stepParam => {
    if (examplesParamsKeys.indexOf(stepParam) != -1) {
      exists = true;
      return;
    }
  });
  return exists;
}

export function highlightStepParams(stringToHighlight: string, parameters: any): string {
  let highlightDescription = stringToHighlight;
  Object.keys(parameters).forEach(paramKey => {
    const paramValue: string = parameters[paramKey];
    if (isNullOrBlankString(paramValue)) {
      highlightDescription = highlightDescription.replace(getStepParamRegExp(paramKey), '<span class="step-param-highlight">$&</span>');
    } else {
      highlightDescription = highlightDescription.replace(getStepParamRegExp(paramKey), '<span class="step-param-highlight-value">' + escapeHtml(paramValue) + '</span>');
    }
  });
  return highlightDescription;
}

export function highlightUnknownParams(stringToHighlight: string): string {
    let highlightDescription = stringToHighlight;
    const matches = macthStepParam(stringToHighlight);
    if (matches) {
      matches.forEach(match => {
        highlightDescription = highlightDescription.replace(getStepParamRegExp(match, '</span>'), '<span class="step-param-highlight-no-value">$&</span>');
      });
    }
    return highlightDescription;
}

export function stepsParamsFromFunctionStep(step: FunctionalStep): Array<string> {

  const descriptionMatches = macthStepParam(step.sentence);
  const implementationMatches = (step.implementation == null ? [] :
    macthStepParam(step.implementation.task) || []);

  return descriptionMatches.concat(implementationMatches);
}

export function allStepsParamsFromFunctionStep(step: FunctionalStep): Array<string> {
  let allMatches = stepsParamsFromFunctionStep(step);

  step.subSteps.forEach(subStep => {
    allMatches = allMatches.concat(allStepsParamsFromFunctionStep(subStep));
  });

  return distinct(allMatches);
}

function macthStepParam(str: string): Array<string> {
  let stepParamMatchRegex = /\*\*(.*?)\*\*/ig;
  const matches: Array<string> = [];
  let match;
  while ((match = stepParamMatchRegex.exec(str)) !== null) {
    matches.push(match[1]);
  }
  return matches;
}

export function focusOnElement(elem: Element) {
  if (elem) {
    const htmlElem = elem as HTMLElement;
    if (htmlElem.focus) {
      htmlElem.focus();
    }
  }
}
