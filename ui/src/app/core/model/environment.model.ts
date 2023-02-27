import { Entry } from './entry.model';

export class Environment {
    constructor(
        public name: string,
        public description: string,
        public targets: Target [] = []) { }
}

export class Target {
    constructor(
        public name: string,
        public url: string,
        public properties: Entry [] = [],
        public environment: string = null,
    ) { }
}

export class TargetFilter {
    constructor(
        public name: string = null,
        public environment: string = null,
    ) { }
}
