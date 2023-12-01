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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { LinkifierService, LoginService } from '@core/services';
import { LayoutOptions } from '@core/layout/layout-options.service';

@Component({
    selector: 'chutney-parent',
    templateUrl: './parent.component.html',
    styleUrls: ['./parent.component.scss']
})
export class ParentComponent implements OnInit, OnDestroy {

    private linkifierSubscription: Subscription;

    constructor(public layoutOptions: LayoutOptions,
                private linkifierService: LinkifierService) {
        this.linkifierSubscription = this.linkifierService.loadLinkifiers().subscribe();

    }

    ngOnInit(): void {
    }

    toggleSidebarMobile() {
        this.layoutOptions.toggleSidebarMobile = !this.layoutOptions.toggleSidebarMobile;
    }

    ngOnDestroy() {
        if (this.linkifierSubscription) {
            this.linkifierSubscription.unsubscribe();
        }
    }

}
