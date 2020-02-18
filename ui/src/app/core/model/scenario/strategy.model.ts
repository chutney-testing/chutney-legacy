import { areEquals, Equals } from '@shared/equals';
import { cloneAsPossible, Clonable } from '@shared/clonable';

export class Strategy implements Equals<Strategy>, Clonable<Strategy> {

  constructor(
    public type: string,
    public parameters: Object
  ) {
  }

  public equals(obj: Strategy): boolean {
    return obj && areEquals(this.type, obj.type) && areEquals(this.parameters, obj.parameters);
  }

  public clone(): Strategy {
    return new Strategy(
      cloneAsPossible(this.type),
      cloneAsPossible(this.parameters)
    );
  }
}
