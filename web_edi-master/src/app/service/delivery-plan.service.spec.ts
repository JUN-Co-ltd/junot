import { TestBed, inject } from '@angular/core/testing';

import { DeliveryPlanService } from './delivery-plan.service';

describe('DeliveryPlanService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DeliveryPlanService]
    });
  });

  it('should be created', inject([DeliveryPlanService], (service: DeliveryPlanService) => {
    expect(service).toBeTruthy();
  }));
});
