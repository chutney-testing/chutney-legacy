import { Component } from '@angular/core';
import { LinkifierService } from '@core/services';

@Component({
  selector: 'chutney-parent',
  templateUrl: './parent.component.html',
  styleUrls: ['./parent.style.scss'],
})
export class ParentComponent {

  constructor(linkifierService: LinkifierService) {
      linkifierService.loadLinkifiers().subscribe(); // needed to fetch linkifiers into sessionStorage
  }
}
