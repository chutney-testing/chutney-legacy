import { ThemeService } from '@core/theme/theme.service';


export function themeInitializer(themeService: ThemeService): () => void {
    return () => themeService.applyCurrentTheme();
}
