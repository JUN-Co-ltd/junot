import { TestBed, inject } from '@angular/core/testing';

import { MKanmstService } from './m-kanmst.service';

describe('MKanmstService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MKanmstService]
    });
  });

  it('should be created', inject([MKanmstService], (service: MKanmstService) => {
    expect(service).toBeTruthy();
  }));
});
