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
import { CanDeactivate } from '@angular/router';
import { CanDeactivatePage } from './page';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class CanDeactivateGuard implements CanDeactivate<CanDeactivatePage> {
  confirmationText: string;

  constructor(private translate: TranslateService) {
    this.initTranslation();
  }

  canDeactivate(page: CanDeactivatePage): boolean {
    if (page && page.canDeactivatePage && !page.canDeactivatePage()) {
      return confirm(this.confirmationText);
    }
    return true;
  }

  private initTranslation() {
    this.getTranslation();
    this.translate.onLangChange.subscribe(() => {
        this.getTranslation();
    });
  }

  private getTranslation() {
    this.translate.get('global.confirm.page.deactivate').subscribe((res: string) => {
      this.confirmationText = res;
    });
  }
}
