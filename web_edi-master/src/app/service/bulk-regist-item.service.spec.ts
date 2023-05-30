import { TestBed, inject } from '@angular/core/testing';

import { BulkRegistItemService } from './bulk-regist-item.service';

describe('BulkRegistItemService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [BulkRegistItemService]
    });
  });

  it('should be created', inject([BulkRegistItemService], (service: BulkRegistItemService) => {
    expect(service).toBeTruthy();
  }));
});
