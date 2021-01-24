import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Authenticate } from '../../../../../data-models';
import { FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'chutney-testing-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.scss'],
})
export class LoginFormComponent {
  @Output() login = new EventEmitter<Authenticate>();

  loginForm = new FormGroup({
    username: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.required]),
  });

  onSubmit($event) {
    $event.preventDefault();
    Object.values(this.loginForm.controls).forEach((control) =>
      control.markAsDirty()
    );
    if (!this.loginForm.valid) {
      return false;
    }
    this.login.emit({
      username: this.loginForm.value.username,
      password: this.loginForm.value.password,
    } as Authenticate);
  }
}
