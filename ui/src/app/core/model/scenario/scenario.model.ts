import { areEquals, Equals } from '@shared/equals';
import { cloneAsPossible, Clonable } from '@shared/clonable';
import { FunctionalStep } from '@core/model';

export class Scenario implements Equals<Scenario>, Clonable<Scenario> {

    constructor(
        public givens: Array<FunctionalStep> = [new FunctionalStep('', '')],
        public when: FunctionalStep = new FunctionalStep('', ''),
        public thens: Array<FunctionalStep> = [new FunctionalStep('', '')]
    ) { }

    static deserialize(jsonObject: any): Scenario {
        const givensJsonObject = jsonObject.givens;
        const whenJsonObject = jsonObject.when;
        const thensJsonObject = jsonObject.thens;

        return new Scenario(
            givensJsonObject ? givensJsonObject.map(givenJsonObject => FunctionalStep.deserialize(givenJsonObject)) : [],
            whenJsonObject ? FunctionalStep.deserialize(whenJsonObject) : new FunctionalStep('', ''),
            thensJsonObject ? thensJsonObject.map(thenJsonObject => FunctionalStep.deserialize(thenJsonObject)) : [new FunctionalStep('', '')]
        );
    }

    serialize(): any {
        const jsonObject = {};
        jsonObject['givens'] = this.givens.map(functionalStep => functionalStep.serialize());
        jsonObject['when'] = this.when.serialize();
        jsonObject['thens'] = this.thens.map(functionalStep => functionalStep.serialize());
        return jsonObject;
    }

    public equals(obj: Scenario): boolean {
        return obj
            && areEquals(this.givens, obj.givens)
            && areEquals(this.when, obj.when)
            && areEquals(this.thens, obj.thens);
    }

    public clone(): Scenario {
        return new Scenario(
            cloneAsPossible(this.givens),
            cloneAsPossible(this.when),
            cloneAsPossible(this.thens)
        );
    }
}
