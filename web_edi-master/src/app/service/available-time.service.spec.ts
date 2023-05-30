import { TestBed, inject } from '@angular/core/testing';

import { AvailableTimeService } from './available-time.service';

describe('ThresholdService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AvailableTimeService]
    });
  });

  it('should be created', inject([AvailableTimeService], (service: AvailableTimeService) => {
    expect(service).toBeTruthy();
  }));
});
