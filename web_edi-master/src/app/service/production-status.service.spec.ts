import { TestBed, inject } from '@angular/core/testing';

import { ProductionStatusService } from './production-status.service';

describe('ProductionStatusService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProductionStatusService]
    });
  });

  it('should be created', inject([ProductionStatusService], (service: ProductionStatusService) => {
    expect(service).toBeTruthy();
  }));
});
