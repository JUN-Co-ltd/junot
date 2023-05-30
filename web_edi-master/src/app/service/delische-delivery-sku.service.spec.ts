import { TestBed, inject } from '@angular/core/testing';

import { DelischeDeliverySkuService } from './delische-delivery-sku.service';

describe('DeliveryScheduleDeliverySkuService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DelischeDeliverySkuService]
    });
  });

  it('should be created', inject([DelischeDeliverySkuService], (service: DelischeDeliverySkuService) => {
    expect(service).toBeTruthy();
  }));
});
