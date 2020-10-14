import { Component } from '@angular/core';
import { Linkifier, LinkifierService } from '@core/services';

@Component({
  selector: 'chutney-parent',
  templateUrl: './parent.component.html',
  styleUrls: ['./parent.style.scss'],
})
export class ParentComponent {

  constructor(linkifierService: LinkifierService) {
      linkifierService.get().subscribe(
          () => { },
          (error) => { console.error(error.error); }
      );
  }
}
