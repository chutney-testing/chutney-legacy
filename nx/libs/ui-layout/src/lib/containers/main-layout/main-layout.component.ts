import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  Inject,
  Optional,
  ViewEncapsulation,
} from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Router } from '@angular/router';
import { BehaviorSubject, Subscription } from 'rxjs';
import { BreakpointObserver } from '@angular/cdk/layout';
import { OverlayContainer } from '@angular/cdk/overlay';
import { MatSidenav, MatSidenavContent } from '@angular/material/sidenav';


const MOBILE_MEDIAQUERY = 'screen and (max-width: 599px)';
const TABLET_MEDIAQUERY = 'screen and (min-width: 600px) and (max-width: 959px)';
const MONITOR_MEDIAQUERY = 'screen and (min-width: 960px)';

@Component({
  selector: 'app-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  @ViewChild('sidenav', { static: true }) sidenav: MatSidenav;
  @ViewChild('content', { static: true }) content: MatSidenavContent;
  options = {
    navPos: 'side',
    dir: 'ltr',
    theme: 'light',
    showHeader: true,
    headerPos: 'fixed',
    showUserPanel: true,
    sidenavOpened: true,
    sidenavCollapsed: false,
    language: 'en-US',
  };

  private layoutChangesSubscription: Subscription;

  private isMobileScreen = false;
  get isOver(): boolean {
    return this.isMobileScreen;
  }

  private contentWidthFix = true;

  private collapsedWidthFix = true;
  private notify$ = new BehaviorSubject<any>({});
  constructor(
    private router: Router,
    private breakpointObserver: BreakpointObserver,
    private overlay: OverlayContainer,
    private element: ElementRef,
    @Optional() @Inject(DOCUMENT) private document: Document,
  ) {

    this.layoutChangesSubscription = this.breakpointObserver
      .observe([MOBILE_MEDIAQUERY, TABLET_MEDIAQUERY, MONITOR_MEDIAQUERY])
      .subscribe(state => {
        // SidenavOpened must be reset true when layout changes
        this.options.sidenavOpened = true;

        this.isMobileScreen = state.breakpoints[MOBILE_MEDIAQUERY];
        this.options.sidenavCollapsed = state.breakpoints[TABLET_MEDIAQUERY];
        this.contentWidthFix = state.breakpoints[MONITOR_MEDIAQUERY];
      });

  }

  ngOnInit() {
    setTimeout(() => (this.contentWidthFix = this.collapsedWidthFix = false));
  }

  ngOnDestroy() {
    this.layoutChangesSubscription.unsubscribe();
  }

  toggleCollapsed() {
    this.options.sidenavCollapsed = !this.options.sidenavCollapsed;
    this.setNavState('collapsed', this.options.sidenavCollapsed);
  }

  resetCollapsedState(timer = 400) {
    setTimeout(() => {
      this.setNavState('collapsed', this.options.sidenavCollapsed);
    }, timer);
  }

  sidenavCloseStart() {
    this.contentWidthFix = false;
  }

  sidenavOpenedChange(isOpened: boolean) {
    this.options.sidenavOpened = isOpened;
    this.setNavState('opened', isOpened);
    this.setNavState('collapsed', this.options.sidenavCollapsed);
    this.collapsedWidthFix = !this.isOver;
  }

  setNavState(type: string, value: boolean) {
    this.notify$.next({ type, value } as any);
  }

}
