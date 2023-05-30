import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DistributionShipmentListResultComponent } from './distribution-shipment-list-result.component';

describe('DistributionShipmentListResultComponent', () => {
  let component: DistributionShipmentListResultComponent;
  let fixture: ComponentFixture<DistributionShipmentListResultComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DistributionShipmentListResultComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DistributionShipmentListResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
