

export function addOptionalParam(obj: any, key: string, value: any, excludedValue?: any) {
    if (value != null && value !== excludedValue) {
        obj[key] = value;
    }
}

export function addOptionalArrayParam(obj: any, key: string, value: Array<any>) {
    if (value != null && value.length > 0) {
        obj[key] = value;
    }
}
