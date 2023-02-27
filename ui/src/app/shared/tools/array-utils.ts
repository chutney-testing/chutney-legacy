export function newInstance<T>(collection: Array<T>): Array<T> {
    return [].concat(collection);
}

export function distinct<T>(collection: Array<T>,
                            equalityFunction: (val1: T, val2: T) => boolean = (val1, val2) => val1 === val2
): Array<T> {
    const distinctValues = [];

    collection.forEach((value) => {
        if (!contains(distinctValues, value, equalityFunction)) {
            distinctValues.push(value);
        }
    });

    return distinctValues;
}

export function flatMap<T, U>(collection: Array<T>,
                              mappingFunction: (value: T) => Array<U>
): Array<U> {
    return collection.reduce((acc, x) => acc.concat(mappingFunction(x)), []);
}

export function contains<T>(collection: Array<T>, valueToSearch: T,
                            equalityFunction: (val1: T, val2: T) => boolean = (val1, val2) => val1 === val2
): boolean {
    for (let i = 0; i < collection.length; i++) {
        if (equalityFunction(collection[i], valueToSearch)) {
            return true;
        }
    }
    return false;
}

export function containsAll<T>(collection1: Array<T>, collection2: Array<T>,
                               equalityFunction: (val1: T, val2: T) => boolean = (val1, val2) => val1 === val2
): boolean {
    let found = false;
    for (let i = 0; i < collection1.length; i++) {
        found = contains(collection2, collection1[i], equalityFunction);
        if (found === false) {
            return false;
        }
    }
    return found;
}


export function intersection<T>(firstCollection: Array<T>,
                                secondCollection: Array<T>,
                                equalityFunction: (val1: T, val2: T) => boolean = (val1, val2) => val1 === val2
): Array<T> {
    return distinct(
        firstCollection
            .concat(secondCollection)
    )
        .filter((item) =>
            contains(firstCollection, item, equalityFunction)
            && contains(secondCollection, item, equalityFunction)
        );
}

function compare<T>(a: T, b: T): number {
    if (a < b) {
        return -1;
    }
    if (a > b) {
        return 1;
    }
    return 0;
}

export function sortByAndOrder<T>(collection: Array<T>, keyExtractor: (T) => any, reverseOrder: boolean): Array<T> {
    collection.sort((a, b) => {
        if (reverseOrder) {
            return compare(keyExtractor(b), keyExtractor(a));
        }
        return compare(keyExtractor(a), keyExtractor(b));
    });
    return collection;
}

export function pairwise<T>(list: Array<T>): Array<Array<T>> {
    if (list.length < 2) { return []; }
    const first = list[0],
        rest = list.slice(1),
        pairs = rest.map(function (x) {
            return [first, x];
        });
    return pairs.concat(pairwise(rest));
}

function normalize(value) {
    if (Array.isArray(value)) {
        value = value.toString();
    }
    return value.toLowerCase ?
        value.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '') :
        '' + value;
}

export function match(item: any, query: string): boolean {
return !!item && normalize(item).indexOf(normalize(query)) !== -1
}

export function filterOnTextContent(input: any, filter: string, attr: any[]) {
    if (!input || !filter || !attr) { return input; }
    if (attr.length === 0) { return input; }

    return input.filter((item) => {
        return filter === undefined
            || filter === ''
            || attr.map(a => match(item[a], filter))
                .reduce((p, c) => p || c);
    });
}
