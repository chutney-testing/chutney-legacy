import { ScenarioIndex, ScenarioType } from '@model';
import { Pipe, PipeTransform } from '@angular/core';
import { intersection } from '@shared/tools/array-utils';

@Pipe({
    name: 'scenarioSearch'
})
export class ScenarioSearchPipe implements PipeTransform {

    transform(input: any, tags: String[], scenarioTypes: ScenarioType[], noTag: boolean, all: boolean) {
        return all ? input : input.filter((item: ScenarioIndex) => {
            return (this.tagPresent(tags, item) || this.noTagPresent(noTag, item)) && this.scenarioTypePresent(scenarioTypes, item);
        });
    }

    private tagPresent(tags: String[], scenario: ScenarioIndex): boolean {
        return intersection(tags, scenario.tags).length > 0;
    }

    private noTagPresent(noTag: boolean, scenario: ScenarioIndex): boolean {
        return noTag && scenario.tags.length === 0;
    }

    private scenarioTypePresent(scenarioTypes: ScenarioType[], scenario: ScenarioIndex): boolean {
        return intersection(scenarioTypes, [scenario.type]).length > 0;
    }
}
