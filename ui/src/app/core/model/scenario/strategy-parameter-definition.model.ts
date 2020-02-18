import { areEquals, Equals } from '@shared/equals';
import { cloneAsPossible, Clonable } from '@shared/clonable';

export class ParameterDefinition implements Equals<ParameterDefinition>, Clonable<ParameterDefinition> {
    constructor(
        public name: string,
        public type: string
    ) {
    }

    public equals(obj: ParameterDefinition): boolean {
        return obj
            && areEquals(this.name, obj.name)
            && areEquals(this.type, obj.type);
    }

    public clone(): ParameterDefinition {
        return new ParameterDefinition(
            cloneAsPossible(this.name),
            cloneAsPossible(this.type)
        );
    }
}
