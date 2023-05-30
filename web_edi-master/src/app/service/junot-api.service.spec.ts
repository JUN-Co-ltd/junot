import { TestBed, inject } from '@angular/core/testing';

import { JunotApiService } from './junot-api.service';

describe('JunotApiService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JunotApiService]
    });
  });

  it('should be created', inject([JunotApiService], (service: JunotApiService) => {
    expect(service).toBeTruthy();
  }));
});
