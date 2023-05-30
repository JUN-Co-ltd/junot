import { TestBed, inject } from '@angular/core/testing';

import { JunpcCodmstService } from './junpc-codmst.service';

describe('JunpcCodmstService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JunpcCodmstService]
    });
  });

  it('should be created', inject([JunpcCodmstService], (service: JunpcCodmstService) => {
    expect(service).toBeTruthy();
  }));
});
