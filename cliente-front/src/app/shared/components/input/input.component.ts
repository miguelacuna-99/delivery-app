import { Component, input, model } from '@angular/core';

let nextInputId = 0;

@Component({
  selector: 'ui-input',
  templateUrl: './input.component.html',
  styleUrl: './input.component.scss'
})
export class InputComponent {
  readonly label = input<string>('');
  readonly type = input<string>('text');
  readonly placeholder = input<string>('');
  readonly error = input<string | null>(null);
  readonly disabled = input(false);

  readonly value = model<string>('');

  readonly inputId = `ui-input-${nextInputId++}`;

  onInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.value.set(target.value);
  }
}
