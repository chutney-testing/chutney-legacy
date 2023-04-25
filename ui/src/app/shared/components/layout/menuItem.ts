import { Authorization } from '@model';
import { FeatureName } from '@core/feature/feature.model';

export class MenuItem {
    label: string;
    link?: string;
    click?: Function;
    iconClass?: string;
    authorizations?: Authorization[];
    feature?: FeatureName;
    options?: {id: string,label: string} [];
    children?: MenuItem[] = [];
}
