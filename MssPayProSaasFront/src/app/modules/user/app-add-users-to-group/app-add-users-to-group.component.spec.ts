import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppAddUsersToGroupComponent } from './app-add-users-to-group.component';

describe('AppAddUsersToGroupComponent', () => {
  let component: AppAddUsersToGroupComponent;
  let fixture: ComponentFixture<AppAddUsersToGroupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AppAddUsersToGroupComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AppAddUsersToGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
