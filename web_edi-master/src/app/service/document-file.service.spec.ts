import { TestBed, inject } from '@angular/core/testing';

import { DocumentFileService } from './document-file.service';

describe('DocumentFileService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DocumentFileService]
    });
  });

  it('should be created', inject([DocumentFileService], (service: DocumentFileService) => {
    expect(service).toBeTruthy();
  }));
});
