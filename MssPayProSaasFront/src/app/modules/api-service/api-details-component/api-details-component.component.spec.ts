import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApiDetailsComponentComponent } from './api-details-component.component';

describe('ApiDetailsComponentComponent', () => {
  let component: ApiDetailsComponentComponent;
  let fixture: ComponentFixture<ApiDetailsComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ApiDetailsComponentComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ApiDetailsComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
