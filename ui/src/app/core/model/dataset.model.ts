
export class Dataset {
    constructor(
        public name: string = '',
        public description: Array<string> = [],
        public tags: Array<string> = [],
        public lastUpdated: Date,
        public id?: string) {
    }
}
