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

export class ReferentialStep {

  constructor(public id: string,
    public name: string,
    public usage: string,
    public steps?: Array<ReferentialStep>,
    public task?: string
  ) { }

}

export function stepsFromObjects(fromJsonObjects: Array<Object>) {
  return fromJsonObjects.map(value => stepFromObject(value));
}

export function stepFromObject(fromJsonObject: Object) {
  return new ReferentialStep(fromJsonObject['id'],
    fromJsonObject['name'],
    fromJsonObject['usage'],
    fromJsonObject['steps'],
    fromJsonObject['task']
  );
}
