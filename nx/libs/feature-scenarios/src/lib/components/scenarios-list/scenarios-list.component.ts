import {
  Component,
  Input,
  OnInit,
  Output,
  EventEmitter,
  ViewChild,
} from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Subject } from 'rxjs';
import { isNullOrUndefined } from 'util';
import { Scenario } from '@chutney/data-access';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';

@Component({
  selector: 'chutney-scenarios-list',
  templateUrl: './scenarios-list.component.html',
  styleUrls: ['./scenarios-list.component.scss'],
})
export class ScenariosListComponent implements OnInit {
  @Output() edit = new EventEmitter<string>();
  @Output() delete = new EventEmitter<string>();
  @Output() view = new EventEmitter<string>();
  displayedColumns: string[] = ['id', 'title', 'status', 'action'];
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  @ViewChild(MatSort, { static: true }) sort: MatSort;

  private _scenariosDataSource: MatTableDataSource<Scenario> = new MatTableDataSource<Scenario>();
  private _unsubscribe = new Subject<void>();

  @Input() set scenarios(scenarios: Scenario[]) {
    if (!isNullOrUndefined(scenarios)) {
      // set data on data source to input scenarios
      this._scenariosDataSource.data = scenarios;
    }
  }

  ngOnInit(): void {
    this._scenariosDataSource.paginator = this.paginator;
    this._scenariosDataSource.sort = this.sort;
  }

  get scenariosDataSource(): MatTableDataSource<Scenario> {
    return this._scenariosDataSource;
  }

  editScenario(id: string) {
    this.edit.emit(id);
  }

  deleteScenario(id: string) {
    this.delete.emit(id);
  }

  viewScenario(id: string) {
    this.view.emit(id);
  }
}
