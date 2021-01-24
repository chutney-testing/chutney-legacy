import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoService } from '@ngneat/transloco';

@Component({
  selector: 'chutney-translation',
  templateUrl: './translation.component.html',
  styleUrls: ['./translation.component.scss'],
})
export class TranslationComponent implements OnInit {
  // this will extract tuple of translation key and name from enum
  languageList = [
    { id: 'en', label: 'English' },
    { id: 'fr', label: 'Français' },
  ];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private transloco: TranslocoService
  ) {}

  ngOnInit(): void {
    console.log(this.transloco.getAvailableLangs());
    //this.languageList = this.transloco.getAvailableLangs()
  }

  switchLanguage(language: string) {
    this.transloco.setActiveLang(language);
  }
}
