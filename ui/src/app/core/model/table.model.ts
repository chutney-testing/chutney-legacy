export class Table {
    constructor(public columnNames: Array<string>,
        public rows: Array<Row>) { }
}

export class Row {
    constructor(public values: Array<string>) { }
}
