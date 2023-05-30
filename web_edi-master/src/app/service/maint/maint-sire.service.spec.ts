import { TestBed, inject } from '@angular/core/testing';

import { MaintSireService } from './maint-sire.service';

describe('MaintSireService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MaintSireService]
    });
  });

  it('should be created', inject([MaintSireService], (service: MaintSireService) => {
    expect(service).toBeTruthy();
  }));
});
