import { TestBed, inject } from '@angular/core/testing';

import { DelischeOrderService } from './delische-order.service';

describe('DelischeOrderService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DelischeOrderService]
    });
  });

  it('should be created', inject([DelischeOrderService], (service: DelischeOrderService) => {
    expect(service).toBeTruthy();
  }));
});
