import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'chutney-menu-item',
  templateUrl: './menu-item.component.html',
  styleUrls: ['./menu-item.component.scss']
})
export class MenuItemComponent implements OnInit {

    @Input() route: string;
    @Input() style: string;
    @Input() label: string;
    @Input() img_src: string;
    @Input() img_src_selected: string;
    @Input() icon_fa_class: string;
    @Output() clickEvent = new EventEmitter<string>();

  constructor() { }

  ngOnInit() {
  }

  onClick() {
    if (this.route == null) {
        this.clickEvent.emit();
    }
  }

}
