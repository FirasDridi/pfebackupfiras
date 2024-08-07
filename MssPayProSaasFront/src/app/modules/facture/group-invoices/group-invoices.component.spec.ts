import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupInvoicesComponent } from './group-invoices.component';

describe('GroupInvoicesComponent', () => {
  let component: GroupInvoicesComponent;
  let fixture: ComponentFixture<GroupInvoicesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GroupInvoicesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(GroupInvoicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
