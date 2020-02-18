export class TargetId {
    constructor(
        public name: string
    ) {
    }

    public asId() {
        return this.name;
    }

    public htmlDisplay() {
        return '<b>' + this.name + '</b>';
    }
}
