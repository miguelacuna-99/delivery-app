import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-spinner',
  standalone: true,
  imports: [CommonModule],
  template: `<span class="app-spinner" [style.width.px]="size" [style.height.px]="size" aria-label="Cargando"></span>`,
  styleUrl: './spinner.component.scss'
})
export class SpinnerComponent {
  @Input() size = 24;
}
