import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InventoryShipmentListComponent } from './inventory-shipment-list.component';

describe('InventoryShipmentListComponent', () => {
  let component: InventoryShipmentListComponent;
  let fixture: ComponentFixture<InventoryShipmentListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InventoryShipmentListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InventoryShipmentListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
