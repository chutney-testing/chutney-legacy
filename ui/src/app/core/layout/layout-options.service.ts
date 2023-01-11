import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class LayoutOptions {
    sidebarHover = false;
    toggleSidebar = false;
    toggleSidebarMobile = false;
}
