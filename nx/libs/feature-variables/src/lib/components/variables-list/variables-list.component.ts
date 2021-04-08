import { Component, Input, Output, OnInit, EventEmitter } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { timer } from 'rxjs';


@Component({
  selector: 'chutney-variables-list',
  templateUrl: './variables-list.component.html',
  styleUrls: ['./variables-list.component.scss']
})
export class VariablesListComponent implements OnInit {

  @Input()
  set groupNames(groupNames: string[]) {
    this.cancel();
    this.dtSource.data = groupNames;
  }

  @Output()
  edit = new EventEmitter<string>();
  @Output()
  delete = new EventEmitter<number>();
  @Output()
  add = new EventEmitter<string>();
  @Output()
  rename = new EventEmitter<Object>();

  dtSource: MatTableDataSource<string> = new MatTableDataSource<string>();
  renameGroupIndex: number = -1;

  constructor() { }

  ngOnInit(): void {
  }

  editGroup(groupName: string) {
    this.edit.emit(groupName);
  }

  deleteGroup(groupNameIndex: number) {
    this.delete.emit(groupNameIndex);
  }

  addGroup(groupNameToAdd: string) {
    this.add.emit(groupNameToAdd);
  }

  renameGroup(groupName: string, newGroupName: string) {
    this.rename.emit({ groupName: groupName, newGroupName: newGroupName });
  }

  initAddGroup() {
    if (!this.groupNameInput()) {
      let groupNames = [].concat(this.dtSource.data);
      groupNames.splice(0, 0, '');
      this.dtSource.data = groupNames;
    }
    this.groupNameInputFocus();
  }

  initRenameGroup(groupNameIndex: number) {
    this.renameGroupIndex = groupNameIndex;
    this.groupNameInputFocus();
  }

  save() {
    const groupName = this.groupNameInput().value;
    if (this.renameGroupIndex == -1) {
      this.addGroup(groupName);
    } else {
      this.renameGroup(this.dtSource.data[this.renameGroupIndex], groupName);
    }
  }

  cancel() {
    if (this.renameGroupIndex == -1) {
      let groupNames = [].concat(this.dtSource.data);
      groupNames.shift();
      this.dtSource.data = groupNames;
    } else {
      this.renameGroupIndex = -1;
    }
  }

  private groupNameInput(): HTMLInputElement {
    return <HTMLInputElement>document.getElementById('groupNameInput');
  }

  private groupNameInputFocus() {
    timer(1000).subscribe(x => {
      this.groupNameInput().focus()
    });
  }
}
