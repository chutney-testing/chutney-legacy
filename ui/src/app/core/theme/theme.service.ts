import { Injectable } from '@angular/core';
import { Theme } from '@core/theme/theme';

@Injectable({
    providedIn: 'root'
})
export class ThemeService {

    private defaultTheme: string = Theme.FLATLY;
    private darkTheme: string = Theme.DARKLY;

    private readonly style: HTMLLinkElement;

    constructor() {
        this.style = document.createElement('link');
        this.style.rel = 'stylesheet';
        document.head.appendChild(this.style);

        this.setTheme(this.getCurrentTheme());
    }

    private setTheme(theme: string) {
        localStorage.setItem('theme', theme);
        this.style.href = `${theme.toLowerCase()}.css`;
    }

    public getCurrentTheme(): string {
        return localStorage.getItem('theme') as Theme ?? this.defaultTheme;
    }

    public isLight(): boolean {
        return this.getCurrentTheme() === this.defaultTheme;
    }

    public switchTheme() {
        this.setTheme(this.isLight() ? this.darkTheme : this.defaultTheme);
    }
}
