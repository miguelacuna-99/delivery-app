import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type IconName =
  | 'dashboard'
  | 'pedidos'
  | 'entregar'
  | 'notificaciones'
  | 'comercio'
  | 'productos'
  | 'cupones'
  | 'usuarios'
  | 'logout'
  | 'check'
  | 'x'
  | 'clock'
  | 'edit'
  | 'trash'
  | 'more'
  | 'inbox';

/**
 * Set de iconos SVG de trazo simple (stroke, sin relleno) para uso consistente
 * en toda la app: sidebar, navbar, badges de notificaciones, estados vacíos.
 */
@Component({
  selector: 'app-icon',
  standalone: true,
  imports: [CommonModule],
  template: `
    <svg
      class="app-icon"
      [style.width.px]="size"
      [style.height.px]="size"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="1.75"
      stroke-linecap="round"
      stroke-linejoin="round"
      aria-hidden="true"
    >
      @switch (name) {
        @case ('dashboard') {
          <rect x="3" y="3" width="7" height="9" rx="1.5"></rect>
          <rect x="14" y="3" width="7" height="5" rx="1.5"></rect>
          <rect x="14" y="12" width="7" height="9" rx="1.5"></rect>
          <rect x="3" y="16" width="7" height="5" rx="1.5"></rect>
        }
        @case ('pedidos') {
          <rect x="4" y="3" width="16" height="18" rx="2"></rect>
          <path d="M9 3v2h6V3"></path>
          <path d="M8 11h8M8 15h5"></path>
        }
        @case ('entregar') {
          <path d="M20 6 9 17l-5-5"></path>
        }
        @case ('notificaciones') {
          <path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"></path>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
        }
        @case ('comercio') {
          <path d="M3 9.5 12 3l9 6.5"></path>
          <path d="M5 9.5V21h14V9.5"></path>
          <path d="M9 21v-6h6v6"></path>
        }
        @case ('productos') {
          <path d="M21 8 12 3 3 8v8l9 5 9-5V8Z"></path>
          <path d="M3 8l9 5 9-5M12 13v8"></path>
        }
        @case ('cupones') {
          <path
            d="M4 8a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v2a2 2 0 0 0 0 4v2a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-2a2 2 0 0 0 0-4Z"
          ></path>
          <path d="M9 6v12" stroke-dasharray="2.5 2.5"></path>
        }
        @case ('usuarios') {
          <circle cx="9" cy="8" r="3.5"></circle>
          <path d="M3.5 20a5.5 5.5 0 0 1 11 0"></path>
          <path d="M17 9h4M19 7v4"></path>
        }
        @case ('logout') {
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
          <path d="M16 17l5-5-5-5"></path>
          <path d="M21 12H9"></path>
        }
        @case ('check') {
          <path d="M20 6 9 17l-5-5"></path>
        }
        @case ('x') {
          <path d="M18 6 6 18M6 6l12 12"></path>
        }
        @case ('clock') {
          <circle cx="12" cy="12" r="9"></circle>
          <path d="M12 7v5l3 3"></path>
        }
        @case ('edit') {
          <path d="M12 20h9"></path>
          <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5Z"></path>
        }
        @case ('trash') {
          <path d="M3 6h18"></path>
          <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"></path>
        }
        @case ('more') {
          <circle cx="5" cy="12" r="1.25"></circle>
          <circle cx="12" cy="12" r="1.25"></circle>
          <circle cx="19" cy="12" r="1.25"></circle>
        }
        @case ('inbox') {
          <path d="M4 12h4l2 3h4l2-3h4"></path>
          <path d="M5.5 5h13l2 7v7a1 1 0 0 1-1 1H4.5a1 1 0 0 1-1-1v-7Z"></path>
        }
      }
    </svg>
  `
})
export class IconComponent {
  @Input({ required: true }) name!: IconName;
  @Input() size = 18;
}
