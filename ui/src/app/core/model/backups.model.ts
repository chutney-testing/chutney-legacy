import { formatDate } from '@angular/common';

export class Backup {
    constructor(
        public agentsNetwork: boolean,
        public environments: boolean,
        public components: boolean,
        public globalVars: boolean,
        public jiraLinks: boolean,
        public time?: Date) {
    }

    public id(): string {
        return formatDate(this.time, 'yyyyMMddHHmmss', 'fr-FR');
    }
}
