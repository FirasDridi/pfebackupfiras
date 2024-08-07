import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceDetailsPopupComponent } from './service-details-popup.component';

describe('ServiceDetailsPopupComponent', () => {
  let component: ServiceDetailsPopupComponent;
  let fixture: ComponentFixture<ServiceDetailsPopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceDetailsPopupComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ServiceDetailsPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
