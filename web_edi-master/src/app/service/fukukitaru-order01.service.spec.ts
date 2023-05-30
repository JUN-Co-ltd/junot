import { TestBed, inject } from '@angular/core/testing';

import { FukukitaruOrder01Service } from './fukukitaru-order01.service';

describe('FukukitaruOrder01Service', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FukukitaruOrder01Service]
    });
  });

  it('should be created', inject([FukukitaruOrder01Service], (service: FukukitaruOrder01Service) => {
    expect(service).toBeTruthy();
  }));
});
