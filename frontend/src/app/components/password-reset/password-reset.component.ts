import { Component } from '@angular/core';
import { FormGroup, NgForm } from "@angular/forms";
import { PasswordResetDto, } from "../../dtos/user";
import { UserService } from "../../services/user.service";
import { ActivatedRoute, Router } from "@angular/router";
import { ToastrService } from "ngx-toastr";
import { NgxSpinnerService } from "ngx-spinner";

@Component({
  selector: 'app-password-reset',
  templateUrl: './password-reset.component.html',
  styleUrls: ['./password-reset.component.scss']
})
export class PasswordResetComponent {
  form: FormGroup;
  passwordResetDto: PasswordResetDto = {
    password: '',
    repeatPassword: '',
  };
  submitted = false;
  error = false;
  token = '';
  showOrHidePassword: boolean; // Hide/show password


  constructor(private userService: UserService, private router: Router, private route: ActivatedRoute, private notification: ToastrService, private spinner: NgxSpinnerService) {
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.token = params.get('token');
    });
  }

  // toggle function which is called by the user with the visablity button, to show or hide the password
  toggleshowOrHidePassword() {
    this.showOrHidePassword = !this.showOrHidePassword;
  }

  onSubmit(form: NgForm) {
    this.spinner.show();
    if (this.passwordResetDto.password != '' && this.passwordResetDto.password != this.passwordResetDto.repeatPassword) {
      this.spinner.hide();
      this.notification.error("Passwords don't match");
      return;
    }
    if (form.valid) {
      this.userService.changePasswordWithResetToken(this.token, this.passwordResetDto).subscribe({
        next: () => {
          this.spinner.hide();
          this.submitted = true
        },
        error: error => {
          console.error(error);
          this.spinner.hide();
          this.notification.error(error.error, "Password change failed");
          this.submitted = false;
        }
      }
      );
    } else {
      console.error('Invalid input');
    }
  }
}
