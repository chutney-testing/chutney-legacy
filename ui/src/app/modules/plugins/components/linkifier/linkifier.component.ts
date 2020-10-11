import { Component } from '@angular/core';
import { ValidationService } from '../../../../molecules/validation/validation.service';
import { FormControl, FormGroup } from '@angular/forms';
import { Linkifier, LinkifierPluginConfigurationService } from '@core/services';
import { delay } from '@shared/tools';


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


    constructor(private linkifierService: LinkifierPluginConfigurationService,
                private validationService: ValidationService) {
    }

    isValid(): boolean {
        return this.validationService.isValidPattern(this.linkifierForm.value['pattern']) && this.linkifierForm.value['pattern'] !== ''
            && this.validationService.isValidUrl(this.linkifierForm.value['link']) && this.linkifierForm.value['link'] !== '';
    }

    addLinkifier() {
        const linkifier = new Linkifier(this.linkifierForm.value['pattern'], this.linkifierForm.value['link'])
        this.linkifierService.save(linkifier).subscribe(
            (res) => {
                this.notify(this.savedMessage, false);
            },
            (error) => {
                this.notify(error.error, true);
            }
        );
    }

    notify(message: string, isErrorNotification: boolean) {
        (async () => {
            this.isErrorNotification = isErrorNotification;
            this.message = message;
            await delay(3000);
            this.message = null;
        })();
    }
}
