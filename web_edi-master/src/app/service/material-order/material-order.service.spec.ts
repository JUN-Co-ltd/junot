import { TestBed, inject } from '@angular/core/testing';

import { MaterialOrderService } from './material-order.service';

describe('MaterialOrderService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MaterialOrderService]
    });
  });

  it('should be created', inject([MaterialOrderService], (service: MaterialOrderService) => {
    expect(service).toBeTruthy();
  }));
});
