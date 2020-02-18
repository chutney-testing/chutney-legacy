import { Table } from './table.model';

export class Sqlresult {
    constructor(public updatedRows?: number,
        public error?: string,
        public table?: Table) {

    }
}

export function sqlResultFromObject(fromJsonObject: Object) {
    return new Sqlresult(
        fromJsonObject['updatedRows'],
        fromJsonObject['error'],
        fromJsonObject['table']
    );
}
