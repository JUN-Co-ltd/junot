import { TestBed, inject } from '@angular/core/testing';

import { DelischeService } from './delische.service';

describe('DelischeService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DelischeService]
    });
  });

  it('should be created', inject([DelischeService], (service: DelischeService) => {
    expect(service).toBeTruthy();
  }));
});
