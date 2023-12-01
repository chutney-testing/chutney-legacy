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

import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';
import { FeatureService } from '@core/feature/feature.service';
import { AlertService } from '@shared';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
    providedIn: 'root'
})
export class FeaturesGuard implements CanActivate {
    constructor(private featureService: FeatureService,
                private alertService: AlertService,
                private translateService: TranslateService) {
    }
    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        const canAccess = this.featureService.active(route.data['feature']);
        if (!canAccess) {
            this.alertService.error(this.translateService.instant('login.unauthorized'), { timeOut: 0, extendedTimeOut: 0, closeButton: true });
        }
        return canAccess;
    }

}
