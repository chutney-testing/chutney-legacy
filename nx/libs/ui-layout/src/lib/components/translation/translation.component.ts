import { Component, Inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  ChutneyAppLanguage,
  TRANSLATION,
  Translation,
} from '@chutney/feature-i18n';

@Component({
  selector: 'chutney-translation',
  templateUrl: './translation.component.html',
  styleUrls: ['./translation.component.scss'],
})
export class TranslationComponent implements OnInit {
  // this will extract tuple of translation key and name from enum
  translations = Object.entries(ChutneyAppLanguage);

  constructor(private router: Router, private route: ActivatedRoute) {}

  ngOnInit(): void {}

  switchLanguage(language: any) {
    var url = this.router
      .createUrlTree([], { relativeTo: this.route })
      .toString();
    Object.entries(ChutneyAppLanguage).forEach((e) => {
      url = url.replace(`/${e[1]}/`, `/${language}/`);
    });
    this.router.navigateByUrl(url);
  }
}
