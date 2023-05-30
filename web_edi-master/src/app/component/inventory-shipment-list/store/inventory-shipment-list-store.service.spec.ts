import { TestBed, inject } from '@angular/core/testing';

import { InventoryShipmentListStoreService } from './inventory-shipment-list-store.service';

describe('InventoryShipmentListStoreService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [InventoryShipmentListStoreService]
    });
  });

  it('should be created', inject([InventoryShipmentListStoreService], (service: InventoryShipmentListStoreService) => {
    expect(service).toBeTruthy();
  }));
});
