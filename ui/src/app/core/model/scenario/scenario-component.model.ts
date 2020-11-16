import { areEquals, Equals } from '@shared/equals';
import { Clonable, cloneAsPossible } from '@shared/clonable';
import { ComponentTask, KeyValue } from '../component-task.model';

export class ScenarioComponent implements Equals<ScenarioComponent>, Clonable<ScenarioComponent> {

    constructor(
        public id?: string,
        public title: string = 'Title',
        public description: string = 'Description',
        public creationDate?: Date,
        public updateDate?: Date,
        public version?: number,
        public author?: string,
        public componentSteps: Array<ComponentTask> = [],
        public parameters: Array<KeyValue> = [],
        public computedParameters: Array<KeyValue> = [],
        public tags: Array<string> = [],
        public datasetId: string = null
    ) {
    }

    public equals(obj: ScenarioComponent): boolean {
        return obj
            && areEquals(this.title, obj.title)
            && areEquals(this.description, obj.description)
            && areEquals(this.componentSteps, obj.componentSteps)
            && areEquals(this.parameters, obj.parameters)
            && areEquals(this.tags, obj.tags)
            && areEquals(this.computedParameters, obj.computedParameters)
            && areEquals(this.datasetId, obj.datasetId);
    }

    public clone(): ScenarioComponent {
        return new ScenarioComponent(
            cloneAsPossible(this.id), // TODO - Is cloning id is a good idea ?
            cloneAsPossible(this.title),
            cloneAsPossible(this.description),
            cloneAsPossible(this.creationDate),
            cloneAsPossible(this.updateDate),
            cloneAsPossible(this.version),
            cloneAsPossible(this.author),
            cloneAsPossible(this.componentSteps),
            cloneAsPossible(this.parameters),
            cloneAsPossible(this.computedParameters),
            cloneAsPossible(this.tags),
            cloneAsPossible(this.datasetId)
        );
    }
}

