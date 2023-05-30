import { TestBed, inject } from '@angular/core/testing';

import { PurchaseHttpService } from './purchase-http.service';

describe('PurchaseHttpService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PurchaseHttpService]
    });
  });

  it('should be created', inject([PurchaseHttpService], (service: PurchaseHttpService) => {
    expect(service).toBeTruthy();
  }));
});
