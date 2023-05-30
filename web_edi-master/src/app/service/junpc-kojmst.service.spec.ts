import { TestBed, inject } from '@angular/core/testing';

import { JunpcKojmstService } from './junpc-kojmst.service';

describe('JunpcKojmstService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JunpcKojmstService]
    });
  });

  it('should be created', inject([JunpcKojmstService], (service: JunpcKojmstService) => {
    expect(service).toBeTruthy();
  }));
});
