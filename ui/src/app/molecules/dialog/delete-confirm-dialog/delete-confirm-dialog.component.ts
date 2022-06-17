import { Component, EventEmitter, Input, Output, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { disabledBoolean } from '@shared/tools/bool-utils';

@Component({
    selector: 'chutney-delete-confirm-dialog',
    templateUrl: './delete-confirm-dialog.component.html',
    styleUrls: ['./delete-confirm-dialog.component.scss']
})
export class DeleteConfirmDialogComponent {

    modalRef: BsModalRef;
    @Input() dialogMessage: string;
    @Input() type = 'trash-button';
    @Input() label: string;
    @Input() title: string;
    @Input() disabled = false;
    @Input() btnSizeClass: string;
    @Input() btnClassIcon: string;
    @Input() btnColor: string;
    @Output() deleteEvent = new EventEmitter();

    disabledBoolean = disabledBoolean;

    constructor(private modalService: BsModalService) {
    }

    openModal(template: TemplateRef<any>) {
        this.modalRef = this.modalService.show(template, {class: 'modal-sm'});
        document.getElementById('no-btn').focus();
    }

    confirm(): void {
        this.modalRef.hide();
        this.deleteEvent.emit();
    }

    decline(): void {
        this.modalRef.hide();
    }
}
