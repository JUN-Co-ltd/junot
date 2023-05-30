import { TestBed, inject } from '@angular/core/testing';

import { DelischeDeliveryRequestService } from './delische-delivery-request.service';

describe('DelischeDeliveryRequestService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DelischeDeliveryRequestService]
    });
  });

  it('should be created', inject([DelischeDeliveryRequestService], (service: DelischeDeliveryRequestService) => {
    expect(service).toBeTruthy();
  }));
});
