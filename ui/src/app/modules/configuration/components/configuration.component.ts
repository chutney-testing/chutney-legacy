import{Component, OnInit}from '@angular/core';
import {FormGroup, FormBuilder, Validators}from '@angular/forms';
import { TranslateService}from '@ngx-translate/core';

import {delay}from '@shared/tools';
import {ConfigurationService}from '@core/services/configuration.service';
import {Configuration}from '@core/model/configuration.model';
import {ValidationService}from '../../../molecules/validation/validation.service';

@Component({
    selector: 'chutney-configuration',
    templateUrl: './configuration.component.html',
    styleUrls: ['./configuration.component.scss']
})
export class ConfigurationComponent implements OnInit {
    
    configuration: Configuration = new Configuration('','','');
    configurationForm: FormGroup;
    
    message;
    private savedMessage: string;
    isErrorNotification: boolean = false;
    
    constructor(private configurationService: ConfigurationService,
                private translate: TranslateService,
                private validationService: ValidationService,
                private formBuilder: FormBuilder) {}
        
    ngOnInit() {
        this.configurationForm = this.formBuilder.group({
            url: ['', Validators.required],
            username: '',
            password: ''
        });
        
        this.loadConfiguration();
        this.initTranslation();
    } 

    private initTranslation() {
        this.translate.get('global.actions.done.saved').subscribe((res: string) => {
            this.savedMessage = res;
        });
    }

    loadConfiguration(){
        this.configurationService.get().subscribe(
            (config: Configuration) => {
                this.configuration = config;
                this.configurationForm.controls.url.patchValue(config.url);
                this.configurationForm.controls.username.patchValue(config.username);
                this.configurationForm.controls.password.patchValue(config.password);
            },
            (error) => {
                this.notify(error,true);
            }
            );
    }
        
    save() {
        const url = this.configurationForm.value['url'] ? this.configurationForm.value['url'] : '';
        const username = this.configurationForm.value['username'] ? this.configurationForm.value['username'] : '';
        const password = this.configurationForm.value['password'] ? this.configurationForm.value['password'] : '';
        this.configuration = new Configuration(url, username, password);
        
        this.configurationService.save(this.configuration).subscribe(
            (res) => {this.notify(this.savedMessage, false);},
            (error) => {this.notify(error,true);}
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
    
    isValid(): boolean {
        return this.validationService.isValidUrl(this.configurationForm.value['url']) || this.configurationForm.value['url'] === '';
    }
                
}
            