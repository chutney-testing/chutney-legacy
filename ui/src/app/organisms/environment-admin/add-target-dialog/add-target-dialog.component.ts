import { Component, EventEmitter, Output, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { Target } from '@model';
import { ValidationService } from '../../../molecules/validation/validation.service';

@Component({
    selector: 'chutney-add-target-dialog',
    templateUrl: './add-target-dialog.component.html',
    styleUrls: ['./add-target-dialog.component.scss']
})
export class AddTargetDialogComponent {

    @Output()
    onSave = new EventEmitter();

    modalRef: BsModalRef;
    target: Target;


    constructor(private modalService: BsModalService, public validationService: ValidationService) { }

    openModal(template: TemplateRef<any>) {
        this.target = new Target('', '');
        this.modalRef = this.modalService.show(template, { class: '' });
    }

    confirm(): void {
        this.modalRef.hide();
        this.onSave.emit(this.target);
    }

    decline(): void {
        this.modalRef.hide();
    }

    isValid(): boolean {
        return this.validationService.isNotEmpty(this.target.name)
            && this.validationService.isValidUrl(this.target.url);
    }
}
