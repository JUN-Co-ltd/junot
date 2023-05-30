import { TestBed, inject } from '@angular/core/testing';

import { MisleadingRepresentationService } from './misleading-representation.service';

describe('MisleadingRepresetationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MisleadingRepresentationService]
    });
  });

  it('should be created', inject([MisleadingRepresentationService], (service: MisleadingRepresentationService) => {
    expect(service).toBeTruthy();
  }));
});
