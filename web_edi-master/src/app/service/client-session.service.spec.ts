import { TestBed, inject } from '@angular/core/testing';

import { ClientSessionService } from './client-session.service';

describe('ClientSessionService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ClientSessionService]
    });
  });

  it('should be created', inject([ClientSessionService], (service: ClientSessionService) => {
    expect(service).toBeTruthy();
  }));
});
