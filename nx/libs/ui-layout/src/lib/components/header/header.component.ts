import {
  Component,
  Output,
  EventEmitter,
  Input,
  ChangeDetectionStrategy,
  ViewEncapsulation,
} from '@angular/core';
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
export class HeaderComponent {
  @Input() showToggle = true;
  @Input() showBranding = false;

  @Output() toggleSidenav = new EventEmitter<void>();
  @Output() toggleTheme = new EventEmitter<string>();

  sideMenuDisplayed = true;
  siderLeftOpened = true;
  scrolled = false;
  isDarkTheme = false;

  private get screenfull(): screenfull.Screenfull {
    return screenfull as screenfull.Screenfull;
  }

  toggleFullscreen() {
    if (this.screenfull.isEnabled) {
      this.screenfull.toggle();
    }
  }

  changeTheme() {
    this.isDarkTheme = !this.isDarkTheme;
    this.toggleTheme.emit(this.isDarkTheme ? 'dark' : 'light');
  }
}
