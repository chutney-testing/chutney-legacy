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

import { Component, Input, OnChanges } from '@angular/core';


@Component({
  selector: 'chutney-execution-badge',
  templateUrl: './execution-badge.component.html',
  styleUrls: ['./execution-badge.component.scss']
})
export class ExecutionBadgeComponent implements OnChanges {

  @Input() status: String;
  @Input() spin = false;

  status_h: String;
  constructor() { }

  ngOnChanges(): void {

    switch (this.status) {
      case 'SUCCESS':
        this.status_h = 'OK';
        break;
      case 'FAILURE':
        this.status_h = 'KO';
        break;
      case 'RUNNING':
        this.status_h = 'RUNNING';
        break;
      case 'PAUSED':
        this.status_h = 'PAUSE';
        break;
      case 'STOPPED':
        this.status_h = 'STOP';
        break;
      case 'NOT_EXECUTED':
        this.status_h = 'NOT EXECUTED';
        break;
    }
  }

}
