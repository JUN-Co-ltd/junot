import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InventoryShipmentListFormComponent } from './inventory-shipment-list-form.component';

describe('InventoryShipmentListFormComponent', () => {
  let component: InventoryShipmentListFormComponent;
  let fixture: ComponentFixture<InventoryShipmentListFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InventoryShipmentListFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InventoryShipmentListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
