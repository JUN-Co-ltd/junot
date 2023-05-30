import { TestBed, inject } from '@angular/core/testing';

import { MakerReturnListStoreService } from './maker-return-list-store.service';

describe('MakerReturnListStoreService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MakerReturnListStoreService]
    });
  });

  it('should be created', inject([MakerReturnListStoreService], (service: MakerReturnListStoreService) => {
    expect(service).toBeTruthy();
  }));
});
