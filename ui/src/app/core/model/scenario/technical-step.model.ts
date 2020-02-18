import { areEquals, Equals } from '@shared/equals';
import { cloneAsPossible, Clonable } from '@shared/clonable';

export class TechnicalStep implements Equals<TechnicalStep>, Clonable<TechnicalStep> {

  constructor(public task: string = '') { }

  public equals(obj: TechnicalStep): boolean {
    return obj && areEquals(this.task, obj.task);
  }

  public clone(): TechnicalStep {
    return new TechnicalStep(cloneAsPossible(this.task));
  }
}
