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

import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'chutney-forms-search-field',
  templateUrl: './search-field.component.html',
  styleUrls: ['./search-field.component.scss']
})
export class SearchFieldComponent implements OnInit {

    @Input() id: string;
    @Input() placeholder: string;
    @Output() searchInputEvent = new EventEmitter<string>();
    @Input() searchInput:  string;

  constructor() { }

  ngOnInit() {
  }

  fireChangeEvent() {
    this.searchInputEvent.emit(this.searchInput);
  }

  clearSearchInput() {
    this.searchInputEvent.emit('');
  }
}
