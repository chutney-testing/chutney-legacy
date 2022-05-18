import { NgModule } from '@angular/core';
import { SharedModule } from '@shared/shared.module';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { LoginComponent } from './components/login/login.component';
import { ParentComponent } from './components/parent/parent.component';
import { CommonModule } from '@angular/common';
import { OrganismsModule } from '../organisms/organisms.module';
import { TranslateModule } from '@ngx-translate/core';
import { NgZorroAntdModule } from '../ng-zorro-antd.module';

@NgModule({
    declarations: [
        LoginComponent,
        ParentComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        HttpClientModule,
        OrganismsModule,
        RouterModule,
        SharedModule,
        TranslateModule,
        NgZorroAntdModule,
        ReactiveFormsModule
    ],

})
export class CoreModule { }
