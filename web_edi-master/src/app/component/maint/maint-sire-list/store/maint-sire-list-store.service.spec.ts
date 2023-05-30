import { TestBed, inject } from '@angular/core/testing';

import { MaintSireListStoreService } from './maint-sire-list-store.service';

describe('MaintSireListStoreService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MaintSireListStoreService]
    });
  });

  it('should be created', inject([MaintSireListStoreService], (service: MaintSireListStoreService) => {
    expect(service).toBeTruthy();
  }));
});
