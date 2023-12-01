/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Directive, Input, HostListener, ElementRef, OnInit, OnDestroy } from '@angular/core';
import { Observable, BehaviorSubject, Subscription, fromEvent } from 'rxjs';

@Directive({
    selector: '[resize]',
})
export class ResizeDirective implements OnInit, OnDestroy {

    @Input('left') left: HTMLElement;
    @Input('grab') grab: HTMLElement;
    @Input('right') right: HTMLElement;
    @Input('customClass') class: string = 'resizing';

    private grabberMouseDown$: Observable<Event>;
    private mouseDownSubscription: Subscription;
    private resizing$: BehaviorSubject<boolean> = new BehaviorSubject(false);
    private parent: HTMLElement;
    leftWidth: number;
    parentWidth: number;
    pointerX: number;

    constructor(private parentRef: ElementRef<HTMLElement>) {}

    ngOnInit(): void {
        this.parent = this.parentRef.nativeElement;

        this.resizing$.subscribe(v => this.resizeClass());

        this.grabberMouseDown$ = fromEvent(this.parent, 'mousedown');
        this.mouseDownSubscription = this.grabberMouseDown$.subscribe(e => this.onMouseDown(e as MouseEvent));
    }

    ngOnDestroy(): void {
        this.resizing$.unsubscribe();
        this.mouseDownSubscription.unsubscribe();
    }

    onMouseDown(event: MouseEvent) {
        if (event.target == this.grab) {
            this.resizing$.next(true);
            this.leftWidth = this.left.offsetWidth;
            this.parentWidth = this.parent.offsetWidth;
            this.pointerX = event.screenX;
        }
    }

    @HostListener('selectstart', ['$event'])
    onSelectionchange(event: Event) {
        if (this.resizing$.getValue()) {
            event.preventDefault();
        }
    }

    @HostListener('window:mouseup')
    onMouseUp() {
        this.resizing$.next(false);
    }

    @HostListener('window:mousemove', ['$event'])
    onMouseMove(event: MouseEvent) {
        if (this.resizing$.getValue() && event.movementX != 0) {
            event.preventDefault();
            var diff = this.pointerX - event.screenX;
            this.leftWidth = this.leftWidth + event.movementX;
            var leftWidthPer = this.toPercentage(this.leftWidth + diff);
            if (leftWidthPer > 1 && leftWidthPer < 99) {
                this.left.style.width = `${leftWidthPer}%`;
                this.right.style.width = `${100 - leftWidthPer}%`;
            }
            this.pointerX = event.screenX;
        }
    }

    private resizeClass() {
        [this.parent, this.left, this.right, this.grab].forEach(el => this.resizingClass(el, this.resizing$.getValue()));
    }

    private resizingClass(el: HTMLElement, on: boolean) {
        if (on) {
            el.className += ` ${this.class}`;
        } else {
            el.className = el.className.replace(` ${this.class}`, '');
        }
    }

    private toPercentage(width: number): number {
        return 100*width/this.parentWidth;
    }
}
