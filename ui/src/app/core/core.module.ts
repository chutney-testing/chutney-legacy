import { NgModule } from '@angular/core';
import { SharedModule } from '@shared/shared.module';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { LoginComponent } from './components/login/login.component';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ParentComponent } from './components/parent/parent.component';
import { EnvironmentsNamesResolver } from '@core/services/environments-names.resolver';
import { EnvironmentsResolver } from '@core/services/environments.resolver';

@NgModule({
    declarations: [
        LoginComponent,
        ParentComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        HttpClientModule,
        RouterModule,
        SharedModule,
        TranslateModule
    ],
    providers: [EnvironmentsNamesResolver, EnvironmentsResolver]

})
export class CoreModule { }
