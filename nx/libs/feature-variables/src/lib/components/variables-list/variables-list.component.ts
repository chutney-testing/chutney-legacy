import { Component, Input, Output, EventEmitter } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';


@Component({
  selector: 'chutney-variables-list',
  templateUrl: './variables-list.component.html',
  styleUrls: ['./variables-list.component.scss']
})
export class VariablesListComponent {

  @Input()
  set groupNames(groupNames: string[]) {
    this.dtSource.data = groupNames;
  }

  @Output()
  edit = new EventEmitter<string>();
  @Output()
  delete = new EventEmitter<string>();

  dtSource: MatTableDataSource<string> = new MatTableDataSource<string>();

  constructor() { }

  editGroup(groupName: string) {
    this.edit.emit(groupName);
  }

  deleteGroup(groupName: string) {
    this.delete.emit(groupName);
  }
}
