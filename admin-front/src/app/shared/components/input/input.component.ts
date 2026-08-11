import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

let nextInputId = 0;

@Component({
  selector: 'app-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="app-input" [class.app-input--error]="error">
      @if (label) {
        <label [for]="id">{{ label }}</label>
      }
      <input
        [id]="id"
        [type]="type"
        [placeholder]="placeholder"
        [disabled]="disabled"
        [ngModel]="value"
        (ngModelChange)="onValueChange($event)"
        [attr.aria-invalid]="error ? true : null"
      />
      @if (error) {
        <span class="app-input__error">{{ error }}</span>
      }
    </div>
  `,
  styleUrl: './input.component.scss'
})
export class InputComponent {
  @Input() label = '';
  @Input() type = 'text';
  @Input() value: string | number | null = '';
  @Input() placeholder = '';
  @Input() error = '';
  @Input() disabled = false;
  @Output() valueChange = new EventEmitter<string>();

  readonly id = `app-input-${nextInputId++}`;

  onValueChange(value: string): void {
    this.value = value;
    this.valueChange.emit(value);
  }
}
