export class Linkifier {
    constructor(public pattern: string,
                public link: string,
                public id?: string) {
        if (this.id == null) {
            this.id = this.hash(pattern) + this.hash(link);
        }
    }

    hash(s: string) {
        for (var i = 0, h = 0; i < s.length; i++) {
            h = Math.imul(31, h) + s.charCodeAt(i) | 0;
        }
        return h.toString();
    }
}
