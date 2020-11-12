import { Directive, HostListener, Inject } from '@angular/core';
import { AccordionItemDirective } from './accordion-item.directive';

@Directive({
  selector: '[navAccordionToggle]',
})
export class AccordionToggleDirective {
  protected navlink: AccordionItemDirective;

  constructor(@Inject(AccordionItemDirective) navlink: AccordionItemDirective) {
    this.navlink = navlink;
  }

  @HostListener('click', ['$event'])
  onClick(e: MouseEvent) {
    this.navlink.toggle();
  }
}
