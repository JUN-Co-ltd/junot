import { TestBed, inject } from '@angular/core/testing';

import { JunpcSirmstService } from './junpc-sirmst.service';

describe('JunpcKojmstService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JunpcSirmstService]
    });
  });

  it('should be created', inject([JunpcSirmstService], (service: JunpcSirmstService) => {
    expect(service).toBeTruthy();
  }));
});
