import { Directive, ElementRef, Renderer, OnInit } from '@angular/core';

@Directive({
    selector : '[chutneyFocusOnShow]'
  })
export class InputFocusDirective implements OnInit {
    constructor(public renderer: Renderer, public elementRef: ElementRef) {}

    ngOnInit() {
      this.renderer.invokeElementMethod(
        this.elementRef.nativeElement, 'focus', []);
    }
}
