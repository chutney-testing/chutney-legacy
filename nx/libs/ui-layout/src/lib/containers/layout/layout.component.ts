import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserGQL } from '@chutney/data-access';
import { distinctUntilChanged, filter, map, pluck } from 'rxjs/operators';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { ScrollDispatcher } from '@angular/cdk/overlay';
import { ActivationStart, Router } from '@angular/router';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent implements OnInit {
  fullscreen = true;
  siderLeftOpened = true;
  siderRightOpened = false;
  scrolled = false;
  user$: Observable<User>;

  constructor(
    private router: Router,
    private breakpointObserver: BreakpointObserver,
    private scrollDispatcher: ScrollDispatcher,
    private changeDetector: ChangeDetectorRef,
    private userGQL: UserGQL
  ) {}

  ngOnInit() {
    this.user$ = this.userGQL.watch().valueChanges.pipe(pluck('data', 'user'));

    // Listen for route changes
    this.router.events
      .pipe(filter((event) => event instanceof ActivationStart))
      .subscribe((route: ActivationStart) => {
        this.fullscreen = route.snapshot.data.fullscreen === true;

        if (this.isSmallScreen) {
          this.siderLeftOpened = !this.isSmallScreen;
        }
      });

    // Monitor device changes
    this.breakpointObserver
      .observe([Breakpoints.Handset, Breakpoints.Tablet, Breakpoints.Web])
      .subscribe(() => (this.siderLeftOpened = !this.isSmallScreen));

    // Listening page scrolling
    this.scrollDispatcher
      .scrolled()
      .pipe(
        filter((x: any) => x.elementRef),
        map((x: any) => x.elementRef.nativeElement.scrollTop > 0),
        distinctUntilChanged()
      )
      .subscribe((scrolled) => {
        this.scrolled = scrolled;
        this.changeDetector.detectChanges();
      });
  }

  get isSmallScreen(): boolean {
    return this.breakpointObserver.isMatched('(max-width: 768px)');
  }

  get toolbarClass() {
    return {
      transparent: !this.scrolled,
      fixed: this.scrolled,
      'fixed-left':
        !this.isSmallScreen && this.siderLeftOpened && this.scrolled,
      'mat-elevation-z3': this.scrolled,
    };
  }

  logout() {}
}
