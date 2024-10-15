import { Component } from '@angular/core';

@Component({
  selector: 'app-loading-dialog',
  template: `
    <div class="loading-dialog">
      <mat-spinner></mat-spinner>
      <p>Loading...</p>
    </div>
  `,
  styles: [`
    .loading-dialog {
      display: flex;
      flex-direction: column;
      align-items: center;
    }
  `]
})
export class LoadingDialogComponent {}
