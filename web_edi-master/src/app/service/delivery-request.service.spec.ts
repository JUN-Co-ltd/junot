import { TestBed, inject } from '@angular/core/testing';

import { DeliveryRequestService } from './delivery-request.service';

describe('DeliveryRequestService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DeliveryRequestService]
    });
  });

  it('should be created', inject([DeliveryRequestService], (service: DeliveryRequestService) => {
    expect(service).toBeTruthy();
  }));
});
