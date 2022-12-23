export class Backup {
    constructor(
        public backupables: string[],
        public time?: Date,
        public id?: string) {
    }
}
