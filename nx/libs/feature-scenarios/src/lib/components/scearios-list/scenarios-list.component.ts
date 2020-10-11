import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Subject } from 'rxjs';
import { isNullOrUndefined } from 'util';
import { Scenario } from '@chutney/data-access';

@Component({
  selector: 'chutney-scenarios-list',
  templateUrl: './scenarios-list.component.html',
  styleUrls: ['./scenarios-list.component.scss'],
})
export class ScenariosListComponent implements OnInit {

  @Output() edit = new EventEmitter<number>();
  @Output() delete = new EventEmitter<number>();
  displayedColumns: string[] = ['id', 'title', 'status', 'action'];

  constructor() {}

  private _scenariosDataSource: MatTableDataSource<
    Scenario
  > = new MatTableDataSource<Scenario>();
  private _unsubscribe = new Subject<void>();

  @Input() set scenarios(scenarios: Scenario[]) {
    if (!isNullOrUndefined(scenarios)) {
      // set data on data source to input scenarios
      this._scenariosDataSource.data = scenarios;
    }
  }

  ngOnInit(): void {}

  get scenariosDataSource(): MatTableDataSource<Scenario> {
    return this._scenariosDataSource;
  }

  editScenario(id: any) {
    this.edit.emit(id)
  }

  deleteScenario(id: any) {
    this.delete.emit(id)
  }
}
