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
