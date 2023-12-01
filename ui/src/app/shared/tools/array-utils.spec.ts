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

import { filterOnTextContent } from '@shared/tools';

describe('filterOnTextContent function...', () => {

    const undef = void 0;

    describe('should not explode (edges cases)...', () => {

        it('undefined everywhere',() => {
            expect(() => filterOnTextContent(undef, undef, undef)).not.toThrow();
        });

        it('empty object list',() => {
            expect(() => filterOnTextContent([], undef, undef)).not.toThrow();
        });
    });

    describe('with objects list...', () => {
        const objects = [
            { stringKey : 'one', numKey: 666 },
            { stringKey : 'two', numKey: 676 },
            { stringKey : 'three', numKey: 677 }
        ];

        describe('should not explode (edges cases)...', () => {

            it('undefined filter', () => {
                expect(() => filterOnTextContent(objects, undef, undef)).not.toThrow();
            });

            it('empty filter',() => {
                expect(() => filterOnTextContent(objects, '', undef)).not.toThrow();
            });

            it('with filter',() => {
                expect(() => filterOnTextContent(objects, 'filter', undef)).not.toThrow();
            });
        });

        it('should do nothing when no attributes given', () => {
            expect(filterOnTextContent(objects, 'o', [])).toEqual(objects);
        });

        it('should filter on string attribute', () => {
            const expectedFilteredObjects = [
                { stringKey : 'one', numKey: 666 },
                { stringKey : 'two', numKey: 676 }
            ];
            expect(filterOnTextContent(objects, 'o', ['stringKey']))
                .toEqual(expectedFilteredObjects);
        });

        it('should filter on number attribute', () => {
            const expectedFilteredObjects = [
                { stringKey : 'two', numKey: 676 },
                { stringKey : 'three', numKey: 677 }
            ];
            expect(filterOnTextContent(objects, '7', ['numKey']))
                .toEqual(expectedFilteredObjects);
        });
    });

});
