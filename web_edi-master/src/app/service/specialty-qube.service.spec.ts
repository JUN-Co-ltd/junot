import { TestBed, inject } from '@angular/core/testing';

import { SpecialtyQubeService } from './specialty-qube.service';

describe('SpecialtyQubeService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SpecialtyQubeService]
    });
  });

  it('should be created', inject([SpecialtyQubeService], (service: SpecialtyQubeService) => {
    expect(service).toBeTruthy();
  }));
});
