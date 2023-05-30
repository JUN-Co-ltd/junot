import { TestBed, inject } from '@angular/core/testing';

import { ThresholdService } from './threshold.service';

describe('ThresholdService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ThresholdService]
    });
  });

  it('should be created', inject([ThresholdService], (service: ThresholdService) => {
    expect(service).toBeTruthy();
  }));
});
