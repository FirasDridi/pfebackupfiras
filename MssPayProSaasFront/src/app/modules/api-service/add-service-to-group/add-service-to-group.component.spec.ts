import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddServiceToGroupComponent } from './add-service-to-group.component';

describe('AddServiceToGroupComponent', () => {
  let component: AddServiceToGroupComponent;
  let fixture: ComponentFixture<AddServiceToGroupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AddServiceToGroupComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddServiceToGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
