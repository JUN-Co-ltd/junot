import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeliveryPlanSubmitConfirmModalComponent } from './delivery-plan-submit-confirm-modal.component';

describe('DeliveryPlanSubmitConfirmComponent', () => {
  let component: DeliveryPlanSubmitConfirmModalComponent;
  let fixture: ComponentFixture<DeliveryPlanSubmitConfirmModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DeliveryPlanSubmitConfirmModalComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeliveryPlanSubmitConfirmModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
