import { Component, OnInit } from '@angular/core';
import { ValidationService } from '../../../../molecules/validation/validation.service';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Linkifier, LinkifierPluginConfigurationService } from '@core/services';
import { delay } from '@shared/tools';


@Component({
    selector: 'chutney-config-linkifier',
    templateUrl: './linkifier.component.html',
    styleUrls: ['./linkifier.component.scss']
})
export class LinkifierComponent implements OnInit {

    linkifierForm = new FormGroup({
        pattern: new FormControl(),
        link: new FormControl()
    });

    message;
    isErrorNotification: boolean = false;

    linkifiers: Array<Linkifier> = [];

    constructor(private fb: FormBuilder,
                private linkifierService: LinkifierPluginConfigurationService,
                private validationService: ValidationService) {
    }

    ngOnInit() {
        this.linkifierForm = this.fb.group({
            pattern: ['', Validators.required],
            link: ['', Validators.required],
        });

        this.loadLinkifiers();
    }

    private loadLinkifiers() {
        this.linkifierService.get().subscribe(
            (linkifiers: Array<Linkifier>) => {
                this.linkifiers = linkifiers;
            },
            (error) => {
                this.notify(error.error, true);
            }
        );
    }

    isValid(): boolean {
        return this.validationService.isValidPattern(this.linkifierForm.value['pattern']) && this.linkifierForm.value['pattern'] !== ''
            && this.validationService.isValidUrl(this.linkifierForm.value['link']) && this.linkifierForm.value['link'] !== '';
    }

    addLinkifier() {
        const linkifier = new Linkifier(this.linkifierForm.value['pattern'], this.linkifierForm.value['link'])
        this.linkifierService.save(linkifier).subscribe(
            (res) => {
                this.notify('Linkifier added', false);
                this.loadLinkifiers();
            },
            (error) => {
                this.notify(error.error, true);
            }
        );
    }

    remove(i: number) {
        const linkifier = this.linkifiers[i];
        this.linkifierService.delete(linkifier).subscribe(
            (res) => {
                this.notify('Linkifier removed', false);
                this.loadLinkifiers();
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
