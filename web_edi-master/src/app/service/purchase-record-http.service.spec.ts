//PRD_0133 #10181 add JFE start
import { TestBed, inject } from '@angular/core/testing';

import { PurchaseRecordHttpService } from './purchase-record-http.service';

describe('PurchaseHttpService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PurchaseRecordHttpService]
    });
  });

  it('should be created', inject([PurchaseRecordHttpService], (service: PurchaseRecordHttpService) => {
    expect(service).toBeTruthy();
  }));
});
//PRD_0133 #10181 add JFE end
