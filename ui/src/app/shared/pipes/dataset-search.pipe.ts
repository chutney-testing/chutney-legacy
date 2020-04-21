import { Dataset } from '@model';
import { Pipe, PipeTransform } from '@angular/core';
import { containsAll } from '@shared/tools/array-utils';

@Pipe({
    name: 'dataSetSearch'
})
export class DataSetSearchPipe implements PipeTransform {

    transform(input: any, tags: String[]) {
        if (tags.length === 0) {
            return input;
        }

        return input.filter((item: Dataset) => {
            return (this.tagPresent(tags, item));
        });
    }

    private tagPresent(tags: String[], dataset: Dataset): boolean {
        return containsAll(tags, dataset.tags);
    }
}
