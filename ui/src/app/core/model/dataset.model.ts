import { KeyValue } from './component-task.model';
import { areEquals } from '@shared';

export class Dataset {
    constructor(
        public name: string = '',
        public description: string = '',
        public tags: Array<string> = [],
        public lastUpdated: Date,
        public uniqueValues: Array<KeyValue>,
        public multipleValues: Array<Array<KeyValue>>,
        public version?: number,
        public id?: string) {
    }

    getMultipleValueHeader(): Array<string> {
        if (this.multipleValues.length > 0) {
            return this.multipleValues[0].map(v => v.key);
        }
        return [];
    }

    public equals(obj: Dataset): boolean {
        return obj
            && areEquals(this.name, obj.name)
            && areEquals(this.description, obj.description)
            && areEquals(this.tags, obj.tags)
            && areEquals(this.uniqueValues, obj.uniqueValues)
            && areEquals(this.multipleValues, obj.multipleValues);
    }
}
