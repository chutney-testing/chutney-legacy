import { Entry } from './entry.model';

export class EnvironmentMetadata {
    constructor(
        public name: string,
        public description: string,
        public targets: Array<Target> = []) { }
}

export class Target {
    constructor(
        public name: string,
        public url: string,
        public properties: Array<Entry> = [],
        public username?: string,
        public password?: string,
        public keyStore?: string,
        public keyStorePassword?: string,
        public privateKey?: string
    ) { }
}
