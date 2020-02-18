import { ToastrModule } from 'ngx-toastr';
import { DefaultMissingTranslationHandler, HttpLoaderFactory } from './app.translate.factory';
// Core
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// External libs
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateLoader, MissingTranslationHandler } from '@ngx-translate/core';
import { ModalModule } from 'ngx-bootstrap';
import { DragulaModule } from 'ng2-dragula';
// Internal common
import { SharedModule } from './shared/shared.module';
import { AppRoutingModule } from './app-routing.module';
import { CoreModule } from '@core/core.module';
import { AppComponent } from './app.component';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    // Core
    BrowserModule,
    CommonModule,
    AppRoutingModule,
    CoreModule,
    // External libs
    FormsModule,
    HttpClientModule,
    DragulaModule.forRoot(),
    NgbModule.forRoot(),
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      },
      missingTranslationHandler: { provide: MissingTranslationHandler, useClass: DefaultMissingTranslationHandler }
    }),
    ToastrModule.forRoot({
      timeOut: 10000,
      positionClass: 'toast-bottom-right',
      preventDuplicates: true,
    }),
    ModalModule.forRoot(),
    // Internal common
    SharedModule,
  ],
  bootstrap: [AppComponent]
})
export class ChutneyAppModule { }
