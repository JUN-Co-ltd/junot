import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeliverySubmitConfirmModalComponent } from './delivery-submit-confirm-modal.component';

describe('SubmitConfirmModalComponent', () => {
  let component: DeliverySubmitConfirmModalComponent;
  let fixture: ComponentFixture<DeliverySubmitConfirmModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DeliverySubmitConfirmModalComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeliverySubmitConfirmModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
