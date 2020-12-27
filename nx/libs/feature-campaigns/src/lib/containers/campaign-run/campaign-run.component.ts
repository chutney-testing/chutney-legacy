import { Component, OnInit } from '@angular/core';
import { chutneyAnimations } from '@chutney/utils';

@Component({
  selector: 'chutney-campaign-run',
  templateUrl: './campaign-run.component.html',
  styleUrls: ['./campaign-run.component.scss'],
  animations: [chutneyAnimations]
})
export class CampaignRunComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
