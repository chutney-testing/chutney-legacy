import { areEquals, Equals } from '@shared/equals';
import { cloneAsPossible, Clonable } from '@shared/clonable';
import { ParameterDefinition } from '@core/model/scenario/strategy-parameter-definition.model';

export class StrategyDefinition implements Equals<StrategyDefinition>, Clonable<StrategyDefinition> {

    constructor(
        public type: string,
        public parameters: ParameterDefinition[],
        public isDefault: boolean
    ) {
    }

    public equals(obj: StrategyDefinition): boolean {
        return obj
            && areEquals(this.type, obj.type)
            && areEquals(this.parameters, obj.parameters)
            && areEquals(this.isDefault, obj.isDefault);
    }

    public clone(): StrategyDefinition {
        return new StrategyDefinition(
            cloneAsPossible(this.type),
            cloneAsPossible(this.parameters),
            cloneAsPossible(this.isDefault)
        );
    }
}


