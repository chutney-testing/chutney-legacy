import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostBinding,
  Input,
  OnChanges,
  OnInit,
  Renderer2,
  ViewEncapsulation,
} from '@angular/core';

@Component({
  selector: 'chutney-execution-badge',
  templateUrl: './execution-badge.component.html',
  styleUrls: ['./execution-badge.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class ExecutionBadgeComponent implements OnInit, OnChanges {
  cacheClassName: string | null = null;
  @Input() status: string;
  @HostBinding('class.tag') tag = true;

  constructor(private renderer: Renderer2, private elementRef: ElementRef) {}

  ngOnInit(): void {
    this.updateClassMap();
  }

  private updateClassMap() {
    if (this.cacheClassName) {
      this.renderer.removeClass(
        this.elementRef.nativeElement,
        this.cacheClassName
      );
    }
    let color = 'default';
    if (this.status === 'SUCCESS') color = 'success';
    else if (this.status === 'RUNNING' || this.status === 'PAUSED')
      color = 'processing';
    else if (this.status === 'FAILURE') color = 'error';
    else color = 'default';
    this.cacheClassName = `tag-${color}`;
    this.renderer.addClass(this.elementRef.nativeElement, this.cacheClassName);
  }

  ngOnChanges(): void {
    this.updateClassMap();
  }
}
