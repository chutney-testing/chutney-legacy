export class JiraPluginConfiguration {
    constructor(
        public url: string,
        public username: string,
        public password: string,
        public urlProxy: string,
        public userProxy: string,
        public passwordProxy: string) {
    }
}
