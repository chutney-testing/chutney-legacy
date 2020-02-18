import { ViewContainerRef, Directive } from "@angular/core";

@Directive({
  selector: '[implementation-host]',
})
export class ImplementationHostDirective {
  constructor(public viewContainerRef: ViewContainerRef) { }
}
