import {NgForm} from "@angular/forms";
import {CanDeactivatePage} from "./page";

export abstract class FormCanDeactivatePage extends CanDeactivatePage {
  abstract get form(): NgForm;

  canDeactivatePage(): boolean {
    return this.form ? this.form.submitted || !this.form.dirty : true;
  }
}
