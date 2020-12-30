import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Campaign,
  CampaignsGQL,
  DeleteCampaignGQL,
  CampaignsDocument,
  CampaignsQuery,
} from '@chutney/data-access';
import { pluck } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { chutneyAnimations } from '@chutney/utils';
import { TdDialogService } from '@covalent/core/dialogs';

@Component({
  selector: 'chutney-campaigns',
  templateUrl: './campaigns.component.html',
  styleUrls: ['./campaigns.component.scss'],
  animations: [chutneyAnimations],
})
export class CampaignsComponent implements OnInit {
  breadcrumbs: any = [
    { title: 'Home', link: ['/'] },
    { title: 'Campaigns', link: ['/'] },
  ];
  campaigns$: Observable<any>;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private _dialogService: TdDialogService,
    private campaignsGQL: CampaignsGQL,
    private deleteCampaignGQL: DeleteCampaignGQL
  ) {}

  ngOnInit(): void {
    this.campaigns$ = this.campaignsGQL
      .watch()
      .valueChanges.pipe(pluck('data', 'campaigns'));
  }

  onEdit(id: string) {
    this.router.navigate([id, 'edit'], { relativeTo: this.route });
  }

  onDelete(id: string) {
    this._dialogService
      .openConfirm({
        title: 'Confirm',
        message: 'After deletion, the scenario cannot be restored',
        cancelButton: 'Cancel',
        acceptButton: 'Ok',
      })
      .afterClosed()
      .subscribe((accept: boolean) => {
        if (accept) {
          console.log(`delete scenario with id ${id}`);
          this.deleteCampaignGQL
            .mutate(
              { input: id },
              {
                update: (store, result) => {
                  const data: CampaignsQuery = store.readQuery({
                    query: CampaignsDocument,
                  });
                  const index = data.campaigns.findIndex(
                    (scenario) => scenario.id === id
                  );
                  const campaigns = [
                    ...data.campaigns.slice(0, index),
                    ...data.campaigns.slice(index + 1),
                  ];
                  store.writeQuery({
                    query: CampaignsDocument,
                    data: { campaigns },
                  });
                },
              }
            )
            .subscribe();
        }
      });
  }

  onView(id: string) {
    this.router.navigate([id, 'view'], { relativeTo: this.route });
  }

  addCampaign() {
    this.router.navigate(['new'], { relativeTo: this.route });
  }
}
