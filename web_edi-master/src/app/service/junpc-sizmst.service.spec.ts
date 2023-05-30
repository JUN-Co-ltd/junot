import { TestBed, inject } from '@angular/core/testing';

import { JunpcSizmstService } from './junpc-sizmst.service';

describe('JunpcKojmstService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JunpcSizmstService]
    });
  });

  it('should be created', inject([JunpcSizmstService], (service: JunpcSizmstService) => {
    expect(service).toBeTruthy();
  }));
});
