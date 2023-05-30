import { TestBed, inject } from '@angular/core/testing';

import { JunpcHrtmstService } from './junpc-hrtmst.service';

describe('JunpcHrtmstService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JunpcHrtmstService]
    });
  });

  it('should be created', inject([JunpcHrtmstService], (service: JunpcHrtmstService) => {
    expect(service).toBeTruthy();
  }));
});
