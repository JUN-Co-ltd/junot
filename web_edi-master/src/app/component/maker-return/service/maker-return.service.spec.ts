import { TestBed, inject } from '@angular/core/testing';

import { MakerReturnService } from './maker-return.service';

describe('MakerReturnService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MakerReturnService]
    });
  });

  it('should be created', inject([MakerReturnService], (service: MakerReturnService) => {
    expect(service).toBeTruthy();
  }));
});
