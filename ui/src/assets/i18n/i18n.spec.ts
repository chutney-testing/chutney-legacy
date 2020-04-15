import { pairwise } from '@shared/tools';

const TRANSLATIONS = [];
['en', 'fr'].forEach(lang => {
    let t = {};
    t['lang'] = lang;
    t['obj'] = require(`src/assets/i18n/${lang}.json`);
    TRANSLATIONS.push(t);
});

describe('i18n', () => {

    pairwise(TRANSLATIONS).forEach(p => {
        const first = p[0];
        const second = p[1];

        it(`should have same keys in i18n files : (${first.lang},${second.lang})`, () => {
            compareObjectPropertiesNames(first.obj, first.lang, second.obj, second.lang);
        });
    });

    function compareObjectPropertiesNames(o1, l1, o2, l2) {
        const o1Keys = Object.getOwnPropertyNames(o1);
        const o2Keys = Object.getOwnPropertyNames(o2);
        o1Keys.forEach(k => {
            expect(o2[k]).toBeDefined(`${l2}.${k} not defined`);
            if (o2[k] && o1[k] instanceof Object) {
                compareObjectPropertiesNames(o2[k], l2 + '.' + k, o1[k], l1 + '.' + k);
            }
        });
        o2Keys.forEach(k => {
            expect(o1[k]).toBeDefined(`${l1}.${k} not defined`);
            if (o1[k] && o2[k] instanceof Object) {
                compareObjectPropertiesNames(o1[k], l1 + '.' + k, o2[k], l2 + '.' + k);
            }
        });
    }
});
