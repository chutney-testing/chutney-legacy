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


