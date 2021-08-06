import { filterOnTextContent } from '@shared/tools';

describe('filterOnTextContent function...', () => {

    var undef;

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
