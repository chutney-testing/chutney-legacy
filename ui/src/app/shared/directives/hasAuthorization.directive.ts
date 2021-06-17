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

    @Input() set hasAuthorization(a: any) {
        const authorizations = a['authorizations'] || (Array.isArray(a) ? a : []);
        const user = a['user'];
        const not: boolean = a['not'] || false;

        const hasAuthorization = this.loginService.hasAuthorization(authorizations, user);
        if ((not && !hasAuthorization) || (!not && hasAuthorization)) {
            // Add template to DOM
            this.viewContainer.createEmbeddedView(this.templateRef);
        } else {
            // Remove template from DOM
            this.viewContainer.clear();
        }
    }
}
