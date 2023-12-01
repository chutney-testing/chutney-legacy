/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { contains, newInstance } from '@shared/tools/array-utils';

export class SelectableTags<T> {
    private allTags: Array<T> = [];
    private selectedTags: Array<T> = [];
    private noTag = true;

    initialize(allItems: Array<T>) {
        this.allTags = allItems;
        this.selectAll();
    }

    selected(): Array<T> {
        return this.selectedTags;
    }

    all(): Array<T> {
        return this.allTags;
    }

    selectAll() {
        this.selectedTags = newInstance(this.allTags);
        this.noTag = true;
    }

    deselectAll() {
        this.selectedTags = [];
        this.noTag = false;
    }

    isSelected(item: T) {
        return contains(this.selectedTags, item);
    }

    toggleSelect(item: T) {
        if (this.selectedTags.length === this.allTags.length) {
            this.selectedTags = [item];
            this.noTag = false;
        } else {
            if (this.isSelected(item)) {
                this.selectedTags.splice(this.selectedTags.indexOf(item), 1);
                this.selectedTags = newInstance(this.selectedTags);
            } else {
                this.selectedTags.push(item);
                this.selectedTags = newInstance(this.selectedTags);
            }
        }
    }

    selectTags(items: Array<T>) {
        this.selectedTags = items;
    }

    setNoTag(noTag) {
        this.noTag = noTag;
    }

    toggleNoTag() {
        this.noTag = !this.noTag;
    }

    isSelectAll() {
        return this.noTag && this.all().length === this.selected().length;
    }

    isNoTagSelected() {
        return this.noTag;
    }
}
