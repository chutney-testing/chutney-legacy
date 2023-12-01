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

export abstract class Clonable<T> {
    abstract clone(): T;
}

// Unlike classes, interfaces exist only at compile-time. It is not possible to do a common instanceof check
export function instanceOfClonable<T>(obj: any): obj is Clonable<T> {
    return 'clone' in obj;
}

export function cloneAsPossible(val: any): any {
    if (val == null) return val;

    if (typeof val === 'object') {
        if (val instanceof Array) {
            const result = [];
            val.forEach((child) => {
                result.push(cloneAsPossible(child));
            });
            return result;
        }
        if (val instanceof Map) {
            const result = new Map();
            val.forEach((value, key) => {
                result.set(cloneAsPossible(key), cloneAsPossible(value));
            });
            return result;
        }

        if (instanceOfClonable(val)) return val.clone();
    }

    return val;
}
