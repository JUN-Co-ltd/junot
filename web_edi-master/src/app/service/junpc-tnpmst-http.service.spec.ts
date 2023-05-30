import { TestBed, inject } from '@angular/core/testing';

import { JunpcTnpmstHttpService } from './junpc-tnpmst-http.service';

describe('JunpcTnpmstHttpService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JunpcTnpmstHttpService]
    });
  });

  it('should be created', inject([JunpcTnpmstHttpService], (service: JunpcTnpmstHttpService) => {
    expect(service).toBeTruthy();
  }));
});
