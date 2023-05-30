import { TestBed, inject } from '@angular/core/testing';

import { DistributionShipmentListStoreService } from './distribution-shipment-list-store.service';

describe('DistributionShipmentListStoreService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DistributionShipmentListStoreService]
    });
  });

  it('should be created', inject([DistributionShipmentListStoreService], (service: DistributionShipmentListStoreService) => {
    expect(service).toBeTruthy();
  }));
});
