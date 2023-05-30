import { TestBed, inject } from '@angular/core/testing';

import { MakerReturnsHttpService } from './maker-returns-http.service';

describe('MakerReturnsHttpService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MakerReturnsHttpService]
    });
  });

  it('should be created', inject([MakerReturnsHttpService], (service: MakerReturnsHttpService) => {
    expect(service).toBeTruthy();
  }));
});
