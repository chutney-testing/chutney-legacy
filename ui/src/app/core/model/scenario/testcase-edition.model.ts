export class TestCaseEdition {
    constructor(
        public testCaseId: string,
        public testCaseVersion: number,
        public editionStartDate: Date,
        public editionUser: string) {
    }
}
