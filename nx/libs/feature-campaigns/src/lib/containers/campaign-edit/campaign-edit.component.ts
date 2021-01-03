import { Component, OnInit } from '@angular/core';
import { chutneyAnimations } from '@chutney/utils';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { map, pluck, skipWhile } from 'rxjs/operators';
import { combineLatest, Observable } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  Campaign,
  CampaignGQL,
  SaveCampaignGQL,
  Scenario,
  ScenariosGQL,
  UpdateCampaignGQL,
} from '@chutney/data-access';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { MatTableDataSource } from '@angular/material/table';
import { SelectionModel } from '@angular/cdk/collections';
import {
  CdkDragDrop,
  moveItemInArray,
  transferArrayItem,
} from '@angular/cdk/drag-drop';
export interface PeriodicElement {
  name: string;
  position: number;
  weight: number;
  symbol: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  { position: 1, name: 'Hydrogen', weight: 1.0079, symbol: 'H' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 3, name: 'Lithium', weight: 6.941, symbol: 'Li' },
  { position: 4, name: 'Beryllium', weight: 9.0122, symbol: 'Be' },
  { position: 5, name: 'Boron', weight: 10.811, symbol: 'B' },
  { position: 6, name: 'Carbon', weight: 12.0107, symbol: 'C' },
  { position: 7, name: 'Nitrogen', weight: 14.0067, symbol: 'N' },
  { position: 8, name: 'Oxygen', weight: 15.9994, symbol: 'O' },
  { position: 9, name: 'Fluorine', weight: 18.9984, symbol: 'F' },
  { position: 10, name: 'Neon', weight: 20.1797, symbol: 'Ne' },
];
@UntilDestroy()
@Component({
  selector: 'chutney-campaign-edit',
  templateUrl: './campaign-edit.component.html',
  styleUrls: ['./campaign-edit.component.scss'],
  animations: [chutneyAnimations],
})
export class CampaignEditComponent implements OnInit {
  campaignForm: FormGroup;
  id: string;
  isAddMode: boolean;
  loading = false;
  submitted = false;
  campaign$: Observable<Campaign>;
  scenarios$: Observable<Scenario[]>;
  campaign: any;
  breadcrumbs: any = [
    { title: 'Home', link: ['/'] },
    { title: 'Campaigns', link: ['/'] },
    { title: 'View', link: ['../../view'] },
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder,
    private snackBar: MatSnackBar,
    private campaignGQL: CampaignGQL,
    private saveCampaignGQL: SaveCampaignGQL,
    private scenariosGQL: ScenariosGQL,
    private updateCampaignGQL: UpdateCampaignGQL
  ) {}

  ngOnInit(): void {
    this.id = this.route.snapshot.params['id'];
    this.isAddMode = !this.id;

    this.campaignForm = this.formBuilder.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
    });

    // @ts-ignore
    this.scenarios$ = this.scenariosGQL.watch().valueChanges.pipe(
      pluck('data', 'scenarios'),
      skipWhile((val) => val === undefined)
    );

    if (!this.isAddMode) {
      this.campaign$ = this.campaignGQL
        .watch({ campaignId: this.id })
        .valueChanges.pipe(pluck('data', 'campaign'), untilDestroyed(this));
      combineLatest([this.campaign$, this.scenarios$])
        .pipe(
          map(([campaign, scenarios]) => {
            // combineLatest returns an array of values, here we map those values to an object
            return { campaign, scenarios };
          })
        )
        .subscribe((data) => {
          this.campaignForm.patchValue(data.campaign);
          this.campaign = data.campaign;
          // @ts-ignore
          this.scenariosInCampaign = [
            ...data.scenarios.filter((scenario) =>
              data.campaign.scenarios.map((x) => x.id).includes(scenario.id)
            ),
          ];
          // @ts-ignore
          this.availableScenarios = [
            ...data.scenarios.filter(
              (scenario) =>
                !data.campaign.scenarios.map((x) => x.id).includes(scenario.id)
            ),
          ];
        });
    } else {
      this.scenarios$.subscribe((data) => {
        // @ts-ignore
        this.availableScenarios = [...data];
      });
    }
  }
  displayedColumns: string[] = ['select', 'position', 'name'];
  dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);
  selection = new SelectionModel<PeriodicElement>(true, []);

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected()
      ? this.selection.clear()
      : this.dataSource.data.forEach((row) => this.selection.select(row));
  }

  /** The label for the checkbox on the passed row */
  checkboxLabel(row?: PeriodicElement): string {
    if (!row) {
      return `${this.isAllSelected() ? 'select' : 'deselect'} all`;
    }
    return `${this.selection.isSelected(row) ? 'deselect' : 'select'} row ${
      row.position + 1
    }`;
  }

  saveCampaign() {
    const campaign = Object.assign({}, this.campaign, this.campaignForm.value, {
      scenarioIds: this.scenariosInCampaign.map((s) => s.id),
    });
    delete campaign.__typename;
    delete campaign.scenarios;
    if (this.isAddMode) {
      this.saveCampaignGQL.mutate({ input: campaign }).subscribe(
        () => {
          const matSnackBarRef = this.snackBar.open('Campaign saved!', 'View');
          matSnackBarRef.onAction().subscribe(() => {
            this.router.navigate([`../view`], { relativeTo: this.route });
          });
        },
        (err) => this.snackBar.open(err.message)
      );
    } else {
      this.updateCampaignGQL.mutate({ input: campaign }).subscribe(
        () => {
          const matSnackBarRef = this.snackBar.open('Campaign saved!', 'View');
          matSnackBarRef.onAction().subscribe(() => {
            this.router.navigate([`../view`], { relativeTo: this.route });
          });
        },
        (err) => this.snackBar.open(err.message)
      );
    }
  }

  scenariosInCampaign: Scenario[] = [];
  availableScenarios: Scenario[] = [];

  // convenience getter for easy access to form fields
  get f(): { [key: string]: AbstractControl } {
    return this.campaignForm.controls;
  }

  drop(event: CdkDragDrop<Scenario[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
    }
  }

  addScenario(item: Scenario) {
    this.scenariosInCampaign = [
      ...this.availableScenarios.splice(
        this.availableScenarios.findIndex((x) => x['id'] == item.id),
        1
      ),
      ...this.scenariosInCampaign,
    ];
  }

  removeScenario(item: Scenario) {
    this.availableScenarios = [
      ...this.scenariosInCampaign.splice(
        this.scenariosInCampaign.findIndex((x) => x['id'] == item.id),
        1
      ),
      ...this.availableScenarios,
    ];
  }
}
