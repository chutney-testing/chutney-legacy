import { Component, Input, EventEmitter, Output } from '@angular/core';
import { AgentInfo } from '@model';

@Component({
  selector: 'chutney-agent',
  templateUrl: './agent.component.html',
  styleUrls: ['./agent.component.scss']
})
export class AgentComponent {

  @Input() configurationAgent: AgentInfo;
  @Output() AgentRemoved = new EventEmitter<AgentInfo>();

  removeAgent() {
    this.AgentRemoved.emit(this.configurationAgent);
  }
}
