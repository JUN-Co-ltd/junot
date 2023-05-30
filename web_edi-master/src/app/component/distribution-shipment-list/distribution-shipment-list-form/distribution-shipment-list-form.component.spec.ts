import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DistributionShipmentListFormComponent } from './distribution-shipment-list-form.component';

describe('DistributionShipmentListFormComponent', () => {
  let component: DistributionShipmentListFormComponent;
  let fixture: ComponentFixture<DistributionShipmentListFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DistributionShipmentListFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DistributionShipmentListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
