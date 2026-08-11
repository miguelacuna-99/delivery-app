import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="app-card">
      @if (title) {
        <h3 class="app-card__title">{{ title }}</h3>
      }
      <div class="app-card__body">
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styleUrl: './card.component.scss'
})
export class CardComponent {
  @Input() title = '';
}
