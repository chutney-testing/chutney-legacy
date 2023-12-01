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
