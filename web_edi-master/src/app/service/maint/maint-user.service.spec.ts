import { TestBed, inject } from '@angular/core/testing';

import { MaintUserService } from './maint-user.service';

describe('MaintUserService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MaintUserService]
    });
  });

  it('should be created', inject([MaintUserService], (service: MaintUserService) => {
    expect(service).toBeTruthy();
  }));
});
