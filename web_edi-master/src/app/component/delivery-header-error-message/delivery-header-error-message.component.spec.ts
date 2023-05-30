import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeliveryHeaderErrorMessageComponent } from './delivery-header-error-message.component';

describe('DeliveryHeaderErrorMessageComponent', () => {
  let component: DeliveryHeaderErrorMessageComponent;
  let fixture: ComponentFixture<DeliveryHeaderErrorMessageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeliveryHeaderErrorMessageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeliveryHeaderErrorMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
