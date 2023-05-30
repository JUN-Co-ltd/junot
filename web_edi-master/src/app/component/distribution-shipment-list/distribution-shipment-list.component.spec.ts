import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DistributionShipmentListComponent } from './distribution-shipment-list.component';

describe('DistributionShipmentListComponent', () => {
  let component: DistributionShipmentListComponent;
  let fixture: ComponentFixture<DistributionShipmentListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DistributionShipmentListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DistributionShipmentListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
