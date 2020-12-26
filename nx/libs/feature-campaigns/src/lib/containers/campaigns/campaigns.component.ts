import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Campaign, CampaignsGQL } from '@chutney/data-access';
import { pluck } from 'rxjs/operators';

@Component({
  selector: 'chutney-campaigns',
  templateUrl: './campaigns.component.html',
  styleUrls: ['./campaigns.component.scss'],
})
export class CampaignsComponent implements OnInit {


  breadcrumbs: any = [
    {title: 'Home', link: ['/']},
    {title: 'Campaigns', link: ['/']},
  ];
  campaigns$: Observable<any>;

  constructor(
    private campaignsGQL: CampaignsGQL
  ) {
  }

  ngOnInit(): void {
    this.campaigns$ = this.campaignsGQL.watch().valueChanges.pipe(pluck('data', 'campaigns'));
  }

  onEdit($event: any) {

  }

  onDelete($event: any) {

  }
}
