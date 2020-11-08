import { ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserGQL } from '@chutney/data-access';
import { distinctUntilChanged, filter, map, pluck } from 'rxjs/operators';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { ScrollDispatcher } from '@angular/cdk/overlay';
import { ActivationStart, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TRANSLATION, Translation } from '@chutney/feature-i18n';

@Component({
  selector: 'app-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss'],
})
export class MainLayoutComponent implements OnInit {
  siderLeftOpened = true;
  siderRightOpened = false;
  scrolled = false;
  user$: Observable<User>;

  constructor(
    @Inject(TRANSLATION) public readonly lang: Translation,
    private router: Router,
    private snackBar: MatSnackBar,
    private breakpointObserver: BreakpointObserver,
    private scrollDispatcher: ScrollDispatcher,
    private changeDetector: ChangeDetectorRef,
    private userGQL: UserGQL
  ) {}

  ngOnInit() {
    this.user$ = this.userGQL.watch().valueChanges.pipe(pluck('data', 'user'));

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

  logout() {
    localStorage.removeItem('user');
    this.snackBar.open('Logged out, Exit successfully');
    this.router.navigate(['/fr/auth/login']);
  }
}
