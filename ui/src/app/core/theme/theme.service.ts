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
        this.themeLink = this.document.querySelector<HTMLLinkElement>(`link[rel="stylesheet"][href="${this.toStyleRel(this.defaultTheme)}"]`);
        const currentTheme = this.getCurrentTheme();
        if (currentTheme !== this.defaultTheme) {
            this.setTheme(currentTheme);
        }
    }

    private setTheme(theme: string) {
        localStorage.setItem('theme', theme);
        this.themeLink.href = this.toStyleRel(theme);
    }

    private toStyleRel(theme: string) {
        return `${theme.toLowerCase()}.css`;
    }
}
