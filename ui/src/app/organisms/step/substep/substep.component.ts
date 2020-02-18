import { Component, OnInit, Input } from '@angular/core';
import { ReferentialStep } from '@model';

@Component({
  selector: 'chutney-substep',
  templateUrl: './substep.component.html',
  styleUrls: ['./substep.component.scss']
})
export class SubstepComponent implements OnInit {

  @Input() step: ReferentialStep;

  constructor() { }

  ngOnInit() {
  }

}
