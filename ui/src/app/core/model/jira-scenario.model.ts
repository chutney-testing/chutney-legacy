export class JiraScenario {
    constructor(
        public id: string,
        public chutneyId: string,
        public lastExecStatus?: string) {
    }
}
