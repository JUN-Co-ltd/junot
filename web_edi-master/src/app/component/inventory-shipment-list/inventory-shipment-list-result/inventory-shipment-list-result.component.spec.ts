import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InventoryShipmentListResultComponent } from './inventory-shipment-list-result.component';

describe('InventoryShipmentListResultComponent', () => {
  let component: InventoryShipmentListResultComponent;
  let fixture: ComponentFixture<InventoryShipmentListResultComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InventoryShipmentListResultComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InventoryShipmentListResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
