import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserInvoicesNewComponent } from './user-invoices-new.component';

describe('UserInvoicesNewComponent', () => {
  let component: UserInvoicesNewComponent;
  let fixture: ComponentFixture<UserInvoicesNewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserInvoicesNewComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UserInvoicesNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
