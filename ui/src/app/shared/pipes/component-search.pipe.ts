import { Pipe, PipeTransform } from '@angular/core';
import { intersection } from '@shared/tools/array-utils';

@Pipe({
    name: 'componentSearch'
})
export class ComponentSearchPipe implements PipeTransform {

    transform(input: any, tags: String[], noTag: boolean, all: boolean) {
        return all ? input : input.filter((item: any) => {
            return (this.tagPresent(tags, item) || this.noTagPresent(noTag, item));
        });
    }

    private tagPresent(tags: String[], item: any): boolean {
        return intersection(tags, item.tags).length > 0;
    }

    private noTagPresent(noTag: boolean, item: any): boolean {
        return noTag && item.tags.length === 0;
    }

}
