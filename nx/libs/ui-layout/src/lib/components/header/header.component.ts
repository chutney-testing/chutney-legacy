import {
  Component,
  OnInit,
  Output,
  EventEmitter,
  Input,
  ChangeDetectionStrategy,
  ViewEncapsulation,
  ChangeDetectorRef,
  Inject,
} from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserGQL } from '@chutney/data-access';
import { distinctUntilChanged, filter, map, pluck } from 'rxjs/operators';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { ScrollDispatcher } from '@angular/cdk/overlay';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TRANSLATION, Translation } from '@chutney/feature-i18n';
import * as screenfull from 'screenfull';

@Component({
  host: {
    class: 'chutney-header',
  },
  selector: 'chutney-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderComponent implements OnInit {
  @Input() showToggle = true;
  @Input() showBranding = false;

  @Output() toggleSidenav = new EventEmitter<void>();
  @Output() toggleSidenavNotice = new EventEmitter<void>();

  sideMenuDisplayed = true;

  private get screenfull(): screenfull.Screenfull {
    return screenfull as screenfull.Screenfull;
  }

  toggleFullscreen() {
    if (this.screenfull.isEnabled) {
      this.screenfull.toggle();
    }
  }

  siderLeftOpened = true;
  scrolled = false;

  constructor(@Inject(TRANSLATION) public readonly lang: Translation) {}

  ngOnInit() {}

  toggleMenu() {
    this.sideMenuDisplayed = !this.sideMenuDisplayed;
    this.toggleSidenav.emit();
  }
}
