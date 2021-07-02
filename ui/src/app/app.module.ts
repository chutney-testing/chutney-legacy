// Core
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// External libs
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { ModalModule } from 'ngx-bootstrap';
import { ToastrModule } from 'ngx-toastr';
import { DragulaModule } from 'ng2-dragula';
// Internal common
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { DefaultMissingTranslationHandler, HttpLoaderFactory } from './app.translate.factory';
import { SharedModule } from '@shared/shared.module';
import { CoreModule } from '@core/core.module';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    // Core
    BrowserModule,
    BrowserAnimationsModule,
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
      positionClass: 'toast-top-full-width',
      preventDuplicates: true,
    }),
    ModalModule.forRoot(),
    // Internal common
    SharedModule,
  ],
  bootstrap: [AppComponent]
})
export class ChutneyAppModule { }
