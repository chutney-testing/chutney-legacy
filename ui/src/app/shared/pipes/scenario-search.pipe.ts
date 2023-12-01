/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { ScenarioIndex } from '@model';
import { Pipe, PipeTransform } from '@angular/core';
import { intersection } from '@shared/tools/array-utils';

@Pipe({
    name: 'scenarioSearch'
})
export class ScenarioSearchPipe implements PipeTransform {

    transform(input: any, tags: String[], noTag: boolean, all: boolean) {
        return all ? input : input.filter((item: ScenarioIndex) => {
            return (this.tagPresent(tags, item) || this.noTagPresent(noTag, item));
        });
    }

    private tagPresent(tags: String[], scenario: ScenarioIndex): boolean {
        return intersection(tags, scenario.tags).length > 0;
    }

    private noTagPresent(noTag: boolean, scenario: ScenarioIndex): boolean {
        return noTag && scenario.tags.length === 0;
    }
}
