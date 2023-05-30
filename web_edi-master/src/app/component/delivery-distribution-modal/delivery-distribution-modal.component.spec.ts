import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeliveryDistributionModalComponent } from './delivery-distribution-modal.component';

describe('DeliveryDistributionModalComponent', () => {
  let component: DeliveryDistributionModalComponent;
  let fixture: ComponentFixture<DeliveryDistributionModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeliveryDistributionModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeliveryDistributionModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
