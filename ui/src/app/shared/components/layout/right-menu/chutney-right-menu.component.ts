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

import { Component, Input } from '@angular/core';
import { MenuItem } from '@shared/components/layout/menuItem';

@Component({
  selector: 'chutney-chutney-right-menu',
  templateUrl: './chutney-right-menu.component.html',
  styleUrls: ['./chutney-right-menu.component.scss']
})
export class ChutneyRightMenuComponent {

    @Input() menuItems: MenuItem [] = [];
    constructor() { }

    onItemClick(item: MenuItem) {
        if (item.click) {
            const option = item.options ? item.options[0].id : null;
            option ? item.click(option) : item.click();
        }
    }

    getItemLink(item: MenuItem) {
        return item.link ? [item.link]: []
    }
}
