import { Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';

import { LoginService } from '@core/services';

@Directive({
    selector : '[hasAuthorization]'
  })
export class HasAuthorizationDirective {
    constructor(
        private templateRef: TemplateRef<any>,
        private viewContainer: ViewContainerRef,
        private loginService: LoginService
    ) {}

    @Input() set hasAuthorization(authorization: any) {
        if (this.loginService.hasAuthorization(authorization)) {
            // Add template to DOM
            this.viewContainer.createEmbeddedView(this.templateRef);
        } else {
            // Remove template from DOM
            this.viewContainer.clear();
        }
    }
}
