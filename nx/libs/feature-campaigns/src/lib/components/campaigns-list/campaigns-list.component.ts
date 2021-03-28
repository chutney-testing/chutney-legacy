import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Campaign } from '@chutney/data-access';
import { Subject } from 'rxjs';
import { isNullOrUndefined } from 'util';

@Component({
  selector: 'chutney-campaigns-list',
  templateUrl: './campaigns-list.component.html',
  styleUrls: ['./campaigns-list.component.scss'],
})
export class CampaignsListComponent implements OnInit {
  @Output() edit = new EventEmitter<string>();
  @Output() delete = new EventEmitter<string>();
  @Output() view = new EventEmitter<string>();
  displayedColumns: string[] = ['id', 'title', /*'status',*/ 'action'];
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  @ViewChild(MatSort, { static: true }) sort: MatSort;

  private _campaignsDataSource: MatTableDataSource<Campaign> = new MatTableDataSource<Campaign>();
  private _unsubscribe = new Subject<void>();

  @Input() set campaigns(campaigns: Campaign[]) {
    if (!isNullOrUndefined(campaigns)) {
      // set data on data source to input campaigns
      this._campaignsDataSource.data = campaigns;
    }
  }

  ngOnInit(): void {
    this._campaignsDataSource.paginator = this.paginator;
    this._campaignsDataSource.sort = this.sort;
  }

  get campaignsDataSource(): MatTableDataSource<Campaign> {
    return this._campaignsDataSource;
  }

  editCampaign(id: string) {
    this.edit.emit(id);
  }

  deleteCampaign(id: string) {
    this.delete.emit(id);
  }

  viewCampaign(id: string) {
    this.view.emit(id);
  }
}
