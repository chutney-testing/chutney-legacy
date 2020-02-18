import { Component, Input } from '@angular/core';


@Component({
  selector: 'chutney-toast-info',
  templateUrl: './toast-info.html',
  styleUrls: ['./toast-info.scss']
})
export class ToastInfoComponent {

  @Input() message: String;

  constructor() { }

}
