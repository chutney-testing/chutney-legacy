export class Step {

    constructor(
        public name?: string,
        public target?: string,
        public type?: string,
        public inputs?: Map<string, Object>,
        public steps?: Step[]
    ) {}
}
