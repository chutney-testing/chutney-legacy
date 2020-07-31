import { Clonable, cloneAsPossible } from 'src/app/shared/clonable';
import { Strategy } from '@core/model/scenario';
import { areEquals } from '@shared';

export class ComponentTask implements Clonable<ComponentTask> {
    constructor(
        public name: string,
        public implementation: Implementation,
        public children: Array<ComponentTask>,
        public parameters: Array<KeyValue> = [],
        public computedParameters: Array<KeyValue> = [],
        public tags: Array<String> = [],
        public strategy: Strategy,
        public id?: string,
    ) {
    }

    public clone(): ComponentTask {
        return new ComponentTask(
            cloneAsPossible(this.name),
            cloneAsPossible(this.implementation),
            cloneAsPossible(this.children),
            cloneAsPossible(this.parameters),
            cloneAsPossible(this.computedParameters),
            cloneAsPossible(this.tags),
            cloneAsPossible(this.strategy),
            cloneAsPossible(this.id));
    }
}

export class Implementation implements Clonable<Implementation> {

    static deserialize(jsonObject: any): Implementation {
        if (jsonObject && jsonObject.identifier != null) {
            return new Implementation(
                jsonObject.identifier,
                jsonObject.target,
                jsonObject.hasTarget,
                jsonObject.mapInputs ? jsonObject.mapInputs : [],
                jsonObject.listInputs ? jsonObject.listInputs : [],
                jsonObject.inputs ? jsonObject.inputs : [],
                jsonObject.outputs ? jsonObject.outputs : []
            );
        }
        return null;
    }

    constructor(
        public identifier: string,
        public target: string,
        public hasTarget: boolean,
        public mapInputs: Array<MapInput> = [],
        public listInputs: Array<ListInput> = [],
        public inputs: Array<SimpleInput> = [],
        public outputs: Array<KeyValue>
    ) {
    }

    public clone(): Implementation {
        return new Implementation(
            cloneAsPossible(this.identifier),
            cloneAsPossible(this.target),
            cloneAsPossible(this.hasTarget),
            cloneAsPossible(this.mapInputs),
            cloneAsPossible(this.listInputs),
            cloneAsPossible(this.inputs),
            cloneAsPossible(this.outputs)
        );
    }
}

export class KeyValue implements Clonable<KeyValue> {

    constructor(
        public key: string,
        public value: any
    ) {
    }

    public clone(): KeyValue {
        return new KeyValue(
            cloneAsPossible(this.key),
            cloneAsPossible(this.value)
        );
    }

    public equals(obj: KeyValue): boolean {
        return obj
            && areEquals(this.key, obj.key)
            && areEquals(this.value, obj.value);
    }
}

export class SimpleInput implements Clonable<SimpleInput> {
    constructor(
        public name: string,
        public value: string,
        public type?
    ) {
    }

    public clone(): SimpleInput {
        return new SimpleInput(
            cloneAsPossible(this.name),
            cloneAsPossible(this.value),
            cloneAsPossible(this.type)
        );
    }
}

export class ListInput implements Clonable<ListInput> {
    constructor(
        public name: string,
        public values: Array<Object>,
    ) {
    }

    public clone(): ListInput {
        return new ListInput(
            cloneAsPossible(this.name),
            cloneAsPossible(this.values)
        );
    }
}

export class MapInput implements Clonable<MapInput> {
    constructor(
        public name: string,
        public values: Array<KeyValue>
    ) {
    }

    public clone(): MapInput {
        return new MapInput(
            cloneAsPossible(this.name),
            cloneAsPossible(this.values)
        );
    }
}
