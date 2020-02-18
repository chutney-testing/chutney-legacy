import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  constructor(private toastr: ToastrService) { }

  success(msg: string, title = 'Done') {
    this.toastr.success(msg, title);
  }

  error(msg: string, title = 'Error') {
    this.toastr.error(msg, title);
  }

  warning(msg: string, title = 'Warning') {
    this.toastr.warning(msg, title);
  }
}
