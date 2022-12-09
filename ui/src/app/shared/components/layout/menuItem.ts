import { Authorization } from '@model';

export class MenuItem {
    label: string;
    link?: string;
    click?: Function;
    iconClass?: string;
    authorizations?: Authorization[];
    options?: {id: string,label: string} [];
    children?: MenuItem[] = [];
}
