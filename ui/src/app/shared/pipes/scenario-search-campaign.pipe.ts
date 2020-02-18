import { ScenarioIndex } from '@model';
import { Pipe, PipeTransform } from '@angular/core';
import { containsAll } from '@shared/tools/array-utils';

@Pipe({
    name: 'scenarioCampaignSearch'
})
export class ScenarioCampaignSearchPipe implements PipeTransform {

    transform(input: any, tags: String[]) {
        if (tags.length === 0) {
            return input;
        }

        return input.filter((item: ScenarioIndex) => {
            return (this.tagPresent(tags, item));
        });
    }

    private tagPresent(tags: String[], scenario: ScenarioIndex): boolean {
        return containsAll(tags, scenario.tags);
    }
}
