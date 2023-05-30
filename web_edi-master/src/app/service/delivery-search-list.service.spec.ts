import { TestBed, inject } from '@angular/core/testing';

import { DeliverySearchListService } from './delivery-search-list.service';

describe('DeliverySearchListService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DeliverySearchListService]
    });
  });

  it('should be created', inject([DeliverySearchListService], (service: DeliverySearchListService) => {
    expect(service).toBeTruthy();
  }));
});
