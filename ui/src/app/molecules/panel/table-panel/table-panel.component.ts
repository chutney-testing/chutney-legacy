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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PaginationInstance } from 'ngx-pagination';

import { Table } from '@model';

@Component({
  selector: 'chutney-table-panel',
  templateUrl: './table-panel.component.html',
  styleUrls: ['./table-panel.component.scss']
})
export class TablePanelComponent {

  @Input() table: Table;
  @Input() paginationInstanceConfig: PaginationInstance;
  @Input() paginationControlConfig: Object = {
    maxSize: 7,
    directionLinks: true,
    autoHide: false,
    previousLabel: '',
    nextLabel: '',
    screenReaderPaginationLabel: 'pagination',
    screenReaderPageLabel: 'page',
    screenReaderCurrentLabel: 'current'
  };

  @Output() pageChangeEvent = new EventEmitter();

  pageChange(event: number) {
    this.pageChangeEvent.emit(event);
  }
}
