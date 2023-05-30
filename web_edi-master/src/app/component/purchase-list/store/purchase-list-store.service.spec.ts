import { TestBed, inject } from '@angular/core/testing';

import { PurchaseListStoreService } from './purchase-list-store.service';

describe('PurchaseListStoreService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PurchaseListStoreService]
    });
  });

  it('should be created', inject([PurchaseListStoreService], (service: PurchaseListStoreService) => {
    expect(service).toBeTruthy();
  }));
});
