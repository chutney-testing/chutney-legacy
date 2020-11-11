import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatExpansionModule } from '@angular/material/expansion';
import { CovalentDialogsModule } from '@covalent/core/dialogs';
import { CovalentSearchModule } from '@covalent/core/search';
import { MatChipsModule } from '@angular/material/chips';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { NgxMatSelectSearchModule } from 'ngx-mat-select-search';
import { MatPasswordModule } from './mat-password/mat-password.module';
import { MatSortModule } from '@angular/material/sort';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTreeModule } from '@angular/material/tree';

@NgModule({
  imports: [
    FlexLayoutModule,
    /* Material Modules*/
    MatInputModule,
    MatCardModule,
    MatButtonModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatToolbarModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatSelectModule,
    MatSnackBarModule,
    MatExpansionModule,
    MatChipsModule,
    MatAutocompleteModule,
    MatTreeModule,
    /** Covalent Modules */
    CovalentDialogsModule,
    CovalentSearchModule,
    /*others*/
    NgxMatSelectSearchModule,
    MatPasswordModule,
  ],
  exports: [
    FlexLayoutModule,
    /* Material modules*/
    MatInputModule,
    MatCardModule,
    MatButtonModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatToolbarModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatSelectModule,
    MatSnackBarModule,
    MatExpansionModule,
    MatChipsModule,
    MatAutocompleteModule,
    MatTreeModule,
    /** Covalent Modules */
    CovalentDialogsModule,
    CovalentSearchModule,
    /*others*/
    NgxMatSelectSearchModule,
    MatPasswordModule,
  ],
})
export class UiMaterialModule {}
