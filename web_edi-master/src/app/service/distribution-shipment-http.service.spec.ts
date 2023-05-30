import { TestBed, inject } from '@angular/core/testing';

import { DistributionShipmentHttpService } from './distribution-shipment-http.service';

describe('DistributionShipmentHttpService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DistributionShipmentHttpService]
    });
  });

  it('should be created', inject([DistributionShipmentHttpService], (service: DistributionShipmentHttpService) => {
    expect(service).toBeTruthy();
  }));
});
