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

import { Inject, Injectable } from '@angular/core';
import { Theme } from '@core/theme/theme';
import { DOCUMENT } from '@angular/common';

@Injectable({
    providedIn: 'root'
})
export class ThemeService {

    private defaultTheme: string = Theme.FLATLY;
    private darkTheme: string = Theme.DARKLY;

    private themeLink: HTMLLinkElement;

    constructor(@Inject(DOCUMENT) private document: Document) {
    }

    getCurrentTheme(): string {
        return localStorage.getItem('theme') as Theme ?? this.defaultTheme;
    }

    isLight(): boolean {
        return this.getCurrentTheme() === this.defaultTheme;
    }

    switchTheme() {
        this.setTheme(this.isLight() ? this.darkTheme : this.defaultTheme);
    }

    applyCurrentTheme() {
        const currentTheme = this.getCurrentTheme();
        this.createThemeLinkElement();
        this.themeLink.href = this.toStyleRel(currentTheme);
    }

    private setTheme(theme: string) {
        localStorage.setItem('theme', theme);
        this.themeLink.href = this.toStyleRel(theme);
    }

    private createThemeLinkElement() {
        this.themeLink = this.document.createElement("link");
        this.themeLink.rel = "stylesheet";
        this.document.head.appendChild(this.themeLink);
    }

    private toStyleRel(theme: string) {
        return `${theme.toLowerCase()}.css`;
    }
}
