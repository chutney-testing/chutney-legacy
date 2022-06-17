import { Directive, ElementRef, Renderer2, OnInit } from '@angular/core';

@Directive({
    selector : '[chutneyFocusOnShow]'
  })
export class InputFocusDirective implements OnInit {
    constructor(public renderer: Renderer2, public elementRef: ElementRef) {}

    ngOnInit() {
      this.renderer.selectRootElement(this.elementRef.nativeElement).focus();
    }
}
