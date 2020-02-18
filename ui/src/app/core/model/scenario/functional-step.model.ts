import { addOptionalParam } from '@shared/tools/object-utils';
import { areEquals, Equals } from '@shared/equals';
import { Clonable, cloneAsPossible } from '@shared/clonable';
import { getStepParamRegExp } from '@shared/tools/function-step-utils';
import { TechnicalStep } from '@core/model/scenario/technical-step.model';
import { Strategy } from '@core/model/scenario/strategy.model';
import { ReferentialStep } from '@core/model/referential-step.model';

export class FunctionalStep implements Equals<FunctionalStep>, Clonable<FunctionalStep> {

  constructor(
    public id: string, // transient
    public sentence = '',
    public implementation?: TechnicalStep,
    public strategy?: Strategy,
    public subSteps: Array<FunctionalStep> = []
  ) { }

  static deserialize(jsonObject: any): FunctionalStep {
    const sentence = jsonObject['sentence'];
    const implementation = jsonObject['implementation'];
    const strategy = jsonObject['strategy'];
    const subSteps = jsonObject['subSteps'];

    return new FunctionalStep(
      '',
      sentence ? sentence : '',
      implementation ? new TechnicalStep(implementation.task) : null,
      strategy ? new Strategy(strategy.type, strategy.parameters) : null,
      subSteps ? subSteps.map(subStep => FunctionalStep.deserialize(subStep)) : []
    );
  }

  serialize(): any {
    const jsonObject = {};

    jsonObject['sentence'] = this.sentence;
    addOptionalParam(jsonObject, 'implementation', this.implementation);
    addOptionalParam(jsonObject, 'strategy', this.strategy);
    jsonObject['subSteps'] = this.subSteps.map(subStep => subStep.serialize());

    return jsonObject;
  }

  containsStepParam(stepParamName: string): boolean {
    const regExp = getStepParamRegExp(stepParamName);
    return this.sentence.search(regExp) >= 0 || (this.implementation == null ? false : this.implementation.task.search(regExp) >= 0);
  }

  public equals(obj: FunctionalStep): boolean {
    return obj
      && areEquals(this.sentence, obj.sentence)
      && areEquals(this.implementation, obj.implementation)
      && areEquals(this.strategy, obj.strategy)
      && areEquals(this.subSteps, obj.subSteps);
  }

  public clone(): FunctionalStep {
    return new FunctionalStep(
      cloneAsPossible(this.id),
      cloneAsPossible(this.sentence),
      cloneAsPossible(this.implementation),
      cloneAsPossible(this.strategy),
      cloneAsPossible(this.subSteps)
    );
  }
}

export function mapReferentialStepToFunctionalStep(refStep: ReferentialStep) {
  const step: FunctionalStep = new FunctionalStep(refStep.id, refStep.name);

  if (refStep.task) {
    try {
      const task = JSON.parse(refStep.task);
      if (task.name) {
        step.sentence = task.name;
        delete task.name;
      }
      step.implementation = new TechnicalStep(JSON.stringify(task));
    } catch (error) {
      step.implementation = new TechnicalStep(refStep.task);
    }
  }

  if (refStep.steps) {
    refStep.steps.forEach(
      subStep => step.subSteps.push(mapReferentialStepToFunctionalStep(subStep))
    );
  }

  return step;
}
