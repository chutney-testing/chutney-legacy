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
