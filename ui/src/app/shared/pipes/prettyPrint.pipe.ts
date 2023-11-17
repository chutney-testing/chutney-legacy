import { Pipe, PipeTransform } from '@angular/core';
import { escapeHtml } from '@shared/tools/string-utils';

@Pipe({ name: 'prettyPrint' })
export class PrettyPrintPipe implements PipeTransform {
    transform(value, escapeHtml: boolean = false): string {
        if (value instanceof Array) {
            return (
                '[\n' +
                value.map((v) => this.beautify(v, escapeHtml)).join(',\n') +
                ']\n'
            );
        } else {
            return this.beautify(value, escapeHtml);
        }
    }

    beautify = (content: string, escapeHtmlP: boolean = false) => {
        let r = content;
        try {
            let json = JSON.parse(content);
            if (typeof json === 'string') {
                content = json;
                throw new Error('');
            } else if (Array.isArray(json)) {
                return (
                    '[\n' +
                    json
                        .map((v) => this.beautify(JSON.stringify(v)))
                        .join(',\n') +
                    ']\n'
                );
            }
            return JSON.stringify(json, null, '  ');
        } catch (error) {
            if (content.startsWith('data:image')) {
                return '<img src="' + content + '" />';
            }
            if (content.startsWith('data:')) {
                return (
                    '<a href="' + content + '" >download information data</a>'
                );
            }
            if (content.startsWith('<') || content.includes('<?xml')) {
                r = this.formatXml(content, '  ');
            }
        }

        return escapeHtmlP ? escapeHtml(r) : r;
    };

    formatXml = (input, indent) => {
        indent = indent || '\t'; //you can set/define other ident than tabs

        //PART 1: Add \n where necessary
        const xmlString = input
            .replace(/(<([a-zA-Z]+\b)[^>]*>)(?!<\/\2>|[\w\s])/g, '$1\n') //add \n after tag if not followed by the closing tag of pair or text node
            .replace(/(<\/[a-zA-Z]+[^>]*>)/g, '$1\n') //add \n after closing tag
            .replace(/>\s+(.+?)\s+<(?!\/)/g, '>\n$1\n<') //add \n between sets of angled brackets and text node between them
            .replace(/>(.+?)<([a-zA-Z])/g, '>\n$1\n<$2') //add \n between angled brackets and text node between them
            .replace(/\?></, '?>\n<'); //detect a header of XML

        const xmlArr = xmlString.split('\n'); //split it into an array (for analise each line separately)

        //PART 2: indent each line appropriately
        let tabs = ''; //store the current indentation
        let start = 0; //starting line

        if (/^<[?]xml/.test(xmlArr[0])) start++; //if the first line is a header, ignore it

        for (
            let i = start;
            i < xmlArr.length;
            i++ //for each line
        ) {
            const line = xmlArr[i].replace(/^\s+|\s+$/g, ''); //trim it (just in case)

            if (/^<[/]/.test(line)) {
                //if the line is a closing tag
                tabs = tabs.replace(indent, ''); //remove one indent from the store
                xmlArr[i] = tabs + line; //add the tabs at the beginning of the line
            } else if (/<.*>.*<\/.*>|<.*[^>]\/>/.test(line)) {
                //if the line contains an entire node
                //leave the store as is
                xmlArr[i] = tabs + line; //add the tabs at the beginning of the line
            } else if (/<.*>/.test(line)) {
                //if the line starts with an opening tag and does not contain an entire node
                xmlArr[i] = tabs + line; //add the tabs at the beginning of the line
                tabs += indent; //and add one indent to the store
            } //if the line contain a text node
            else {
                xmlArr[i] = tabs + line; // add the tabs at the beginning of the line
            }
        }

        //PART 3: return formatted string (source)
        return xmlArr.join('\n'); //rejoin the array to a string and return it
    };
}
