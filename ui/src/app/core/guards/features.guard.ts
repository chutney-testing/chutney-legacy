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
