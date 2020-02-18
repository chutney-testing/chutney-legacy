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

export function sortBy<T>(collection: Array<T>, keyExtractor: (T) => any): Array<T> {
    collection.sort((a, b) => compare(keyExtractor(a), keyExtractor(b)));
    return collection;
}

function compare<T>(a: T, b: T): number {
    if (a < b)
        return -1;
    if (a > b)
        return 1;
    return 0;
}
