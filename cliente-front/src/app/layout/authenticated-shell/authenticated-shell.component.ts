import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-authenticated-shell',
  imports: [RouterOutlet, NavbarComponent, FooterComponent],
  templateUrl: './authenticated-shell.component.html',
  styleUrl: './authenticated-shell.component.scss'
})
export class AuthenticatedShellComponent {}
