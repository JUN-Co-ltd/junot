import { TestBed, inject } from '@angular/core/testing';

import { DelischeFileService } from './delische-file.service';

describe('DelischeFileService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DelischeFileService]
    });
  });

  it('should be created', inject([DelischeFileService], (service: DelischeFileService) => {
    expect(service).toBeTruthy();
  }));
});
