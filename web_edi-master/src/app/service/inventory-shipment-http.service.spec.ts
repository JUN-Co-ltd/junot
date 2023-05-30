import { TestBed, inject } from '@angular/core/testing';

import { InventoryShipmentHttpService } from './inventory-shipment-http.service';

describe('InventoryShipmentHttpService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [InventoryShipmentHttpService]
    });
  });

  it('should be created', inject([InventoryShipmentHttpService], (service: InventoryShipmentHttpService) => {
    expect(service).toBeTruthy();
  }));
});
