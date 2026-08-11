import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FooterComponent } from '../footer/footer.component';
import { ToastComponent } from '../../shared/components/toast/toast.component';

@Component({
  selector: 'app-public-shell',
  imports: [RouterOutlet, FooterComponent, ToastComponent],
  templateUrl: './public-shell.component.html',
  styleUrl: './public-shell.component.scss'
})
export class PublicShellComponent {}
