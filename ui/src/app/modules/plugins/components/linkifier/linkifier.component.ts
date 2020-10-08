import { Component } from '@angular/core';
import { ValidationService } from '../../../../molecules/validation/validation.service';
import { FormControl, FormGroup } from '@angular/forms';


@Component({
    selector: 'chutney-config-linkifier',
    templateUrl: './linkifier.component.html',
    styleUrls: ['./linkifier.component.scss']
})
export class LinkifierComponent {

    linkifierForm = new FormGroup({
        pattern: new FormControl(),
        link: new FormControl()

    });

    message;
    private savedMessage: string;
    isErrorNotification: boolean = false;


    constructor(private validationService: ValidationService) {
    }

    isValid(): boolean {
        return this.validationService.isValidPattern(this.linkifierForm.value['pattern']) && this.linkifierForm.value['pattern'] !== ''
            && this.validationService.isValidUrl(this.linkifierForm.value['link']) && this.linkifierForm.value['link'] !== '';
    }

    addLinkifier() {

    }
}
