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

import { Component, HostListener, OnInit } from '@angular/core';
import { LoginService } from '@core/services';
import { LayoutOptions } from '@core/layout/layout-options.service';
import { MenuItem } from '@shared/components/layout/menuItem';
import { allMenuItems } from '@shared/components/layout/left-menu/chutney-left-menu.items';
import { FeatureService } from '@core/feature/feature.service';

@Component({
    selector: 'chutney-chutney-left-menu',
    templateUrl: './chutney-left-menu.component.html',
    styleUrls: ['./chutney-left-menu.component.scss']
})
export class ChutneyLeftMenuComponent implements OnInit {
    public menuItems = allMenuItems;
    private newInnerWidth: number;
    private innerWidth: number;

    constructor(public layoutOptions: LayoutOptions,
                private loginService: LoginService,
                private featureService: FeatureService) {
    }

    ngOnInit(): void {
        setTimeout(() => {
            this.innerWidth = window.innerWidth;
            if (this.innerWidth < 1200) {
                this.layoutOptions.toggleSidebar = true;
            }
        });
    }

    canViewMenuGroup(item: MenuItem): boolean {
        return !!item.children.find(subItem => this.canViewMenuItem(subItem));

    }

    canViewMenuItem(item: MenuItem): boolean {
        return this.loginService.hasAuthorization(item.authorizations) && this.featureService.active(item.feature);
    }

    toggleSidebar() {
        this.layoutOptions.toggleSidebar = !this.layoutOptions.toggleSidebar;
    }

    sidebarHover() {
        this.layoutOptions.sidebarHover = !this.layoutOptions.sidebarHover;
    }

    @HostListener('window:resize', ['$event'])
    onResize(event) {
        this.newInnerWidth = event.target.innerWidth;

        if (this.newInnerWidth < 1200) {
            this.layoutOptions.toggleSidebar = true;
        } else {
            this.layoutOptions.toggleSidebar = false;
        }

    }
}
