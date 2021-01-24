import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ReplaySubject, Subject } from 'rxjs';
import { MatSelect } from '@angular/material/select';
import { Apollo } from 'apollo-angular';
import { map, pluck, take, takeUntil } from 'rxjs/operators';
import { ScenariosGQL } from '@chutney/data-access';
import { tdCollapseAnimation } from '@covalent/core/common';
import { scenariosFilterVar } from '../../cache';

@Component({
  selector: 'chutney-scenarios-search-form',
  templateUrl: './scenarios-search-form.component.html',
  styleUrls: ['./scenarios-search-form.component.scss'],
  animations: [tdCollapseAnimation],
})
export class ScenariosSearchFormComponent implements OnInit, OnDestroy {
  protected tags: string[] = [];

  /** control for the MatSelect filter keyword multi-selection */
  public tagFilterCtrl: FormControl = new FormControl();

  /** list of tags filtered by search keyword */
  public filteredTags: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);

  triggerState = true;

  searchForm: FormGroup;

  @ViewChild('multiSelect', { static: true }) multiSelect: MatSelect;

  /** Subject that emits when the component has been destroyed. */
  protected _onDestroy = new Subject<void>();

  constructor(
    private scenariosGQL: ScenariosGQL,
    private apollo: Apollo,
    private fb: FormBuilder
  ) {
    this.searchForm = this.fb.group({
      text: null,
      tags: null,
      date: null,
    });
    // listen for search field value changes
    this.tagFilterCtrl.valueChanges
      .pipe(takeUntil(this._onDestroy))
      .subscribe(() => {
        this.filterTags();
      });
  }

  ngOnInit(): void {
    this.scenariosGQL
      .watch()
      .valueChanges.pipe(
        pluck('data', 'scenarios'),
        map((scenarios: any[]) => [].concat(...scenarios.map((s) => s.tags)))
      )
      .subscribe((data) => {
        this.tags = [...data.filter((v, idx, self) => self.indexOf(v) === idx)];
        this.filteredTags.next(this.tags.slice());
      });

    this.searchForm.patchValue({
      ...scenariosFilterVar(),
    });
    this.triggerState = !scenariosFilterVar().advanced;

    this.searchForm.valueChanges.subscribe((data) =>
      scenariosFilterVar({ ...data, advanced: !this.triggerState })
    );
  }

  ngOnDestroy() {
    this._onDestroy.next();
    this._onDestroy.complete();
  }

  showFilter() {
    this.triggerState = !this.triggerState;
    scenariosFilterVar({
      ...scenariosFilterVar(),
      advanced: !this.triggerState,
    });
  }

  toggleSelectAll(selectAllValue: boolean) {
    this.filteredTags
      .pipe(take(1), takeUntil(this._onDestroy))
      .subscribe((val) => {
        if (selectAllValue) {
          this.searchForm.controls['tags'].patchValue(val);
        } else {
          this.searchForm.controls['tags'].patchValue([]);
        }
      });
  }

  protected filterTags() {
    if (!this.tags) {
      return;
    }
    // get the search keyword
    let search = this.tagFilterCtrl.value;
    if (!search) {
      this.filteredTags.next(this.tags.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    // filter the tags
    this.filteredTags.next(
      this.tags.filter((tag) => tag.toLowerCase().indexOf(search) > -1)
    );
  }
}
