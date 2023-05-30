import { TestBed, inject } from '@angular/core/testing';

import { MaintCodeService } from './maint-code.service';

describe('MaintCodeService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MaintCodeService]
    });
  });

  it('should be created', inject([MaintCodeService], (service: MaintCodeService) => {
    expect(service).toBeTruthy();
  }));
});
