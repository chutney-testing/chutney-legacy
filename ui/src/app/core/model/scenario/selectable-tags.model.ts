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
