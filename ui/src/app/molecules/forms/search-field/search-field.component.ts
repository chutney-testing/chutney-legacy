import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'chutney-forms-search-field',
  templateUrl: './search-field.component.html',
  styleUrls: ['./search-field.component.scss']
})
export class SearchFieldComponent implements OnInit {

    @Input() id: string;
    @Input() placeholder: string;
    @Output() searchInputEvent = new EventEmitter<string>();
    @Input() searchInput:  string;

  constructor() { }

  ngOnInit() {
  }

  fireChangeEvent() {
    this.searchInputEvent.emit(this.searchInput);
  }

  clearSearchInput() {
    this.searchInputEvent.emit('');
  }
}
