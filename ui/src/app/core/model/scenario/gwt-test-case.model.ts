import { Execution } from './execution.model';
import { ExampleParameters } from './example-parameters.model';
import { addOptionalParam } from '@shared/tools/object-utils';
import { areEquals, Equals } from '@shared/equals';
import { Clonable, cloneAsPossible } from '@shared/clonable';
import { Scenario } from './scenario.model';

export class GwtTestCase implements Equals<GwtTestCase>, Clonable<GwtTestCase> {

    constructor(
        public id?: string,
        public title: string = 'Title',
        public description: string = 'Description',
        public creationDate?: Date,
        public updateDate?: Date,
        public version?: number,
        public author?: string,
        public repositorySource?: string,
        public executions: Array<Execution> = [],
        public tags?: Array<string>,
        public scenario: Scenario = new Scenario(),
        public wrappedParams: ExampleParameters = new ExampleParameters()
    ) {
    }

    static deserialize(jsonObject: any): GwtTestCase {
        return new GwtTestCase(
            jsonObject.id,
            jsonObject.title || 'Title',
            jsonObject.description || 'Description',
            jsonObject.creationDate,
            jsonObject.updateDate,
            jsonObject.version,
            jsonObject.author,
            jsonObject.repositorySource,
            Execution.deserializeExecutions(jsonObject.executions),
            jsonObject.tags,
            Scenario.deserialize(jsonObject.scenario),
            ExampleParameters.deserialize(jsonObject.computedParameters)
        );
    }

    serialize(): any {
        const jsonObject = {};

        addOptionalParam(jsonObject, 'id', this.id);
        jsonObject['title'] = this.title;
        jsonObject['description'] = this.description;
        addOptionalParam(jsonObject, 'creationDate', this.creationDate);
        addOptionalParam(jsonObject, 'updateDate', this.updateDate);
        addOptionalParam(jsonObject, 'version', this.version);
        addOptionalParam(jsonObject, 'author', this.author);
        addOptionalParam(jsonObject, 'repositorySource', this.repositorySource);
        jsonObject['executions'] = this.executions;
        addOptionalParam(jsonObject, 'tags', this.tags);
        jsonObject['scenario'] = this.scenario.serialize();
        addOptionalParam(jsonObject, 'computedParameters', this.wrappedParams.serialize());
        return jsonObject;
    }

    public equals(obj: GwtTestCase): boolean {
        return obj
            && areEquals(this.title, obj.title)
            && areEquals(this.description, obj.description)
            && areEquals(this.tags, obj.tags)
            && areEquals(this.scenario, obj.scenario)
            && areEquals(this.wrappedParams, obj.wrappedParams);
    }

    public clone(): GwtTestCase {
        return new GwtTestCase(
            null,
            cloneAsPossible(this.title),
            cloneAsPossible(this.description),
            null,
            null,
            null,
            null,
            null,
            null,
            cloneAsPossible(this.tags),
            cloneAsPossible(this.scenario),
            cloneAsPossible(this.wrappedParams)
        );
    }
}
