//PRD_0133 #10181 add JFE start
import { TestBed, inject } from '@angular/core/testing';

import { PurchaseRecordListStoreService } from '../store/purchase-record-list-store.service';

describe('PurchaseListStoreService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PurchaseRecordListStoreService]
    });
  });

  it('should be created', inject([PurchaseRecordListStoreService], (service: PurchaseRecordListStoreService) => {
    expect(service).toBeTruthy();
  }));
});
//PRD_0133 #10181 add JFE end
