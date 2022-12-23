import { Injectable } from '@angular/core';
import { Theme } from '@core/theme/theme';

@Injectable({
    providedIn: 'root'
})
export class ThemeService {

    private defaultTheme: string = Theme.FLATLY.toString();
    private readonly style: HTMLLinkElement;

    constructor() {
        this.style = document.createElement('link');
        this.style.rel = 'stylesheet';
        document.head.appendChild(this.style);

        this.switchTheme(this.getCurrentTheme());
    }

    public getCurrentTheme(): string {
        return localStorage.getItem('theme') as Theme ?? this.defaultTheme;
    }

    public switchTheme(theme: string) {
        localStorage.setItem('theme', theme);
        this.style.href = `${theme.toLowerCase()}.css`;
    }
}
