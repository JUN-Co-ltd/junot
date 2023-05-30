import { TestBed, inject } from '@angular/core/testing';

import { FabricInspectionResultService } from './fabric-inspection-result.service';

describe('FabricInspectionResultService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FabricInspectionResultService]
    });
  });

  it('should be created', inject([FabricInspectionResultService], (service: FabricInspectionResultService) => {
    expect(service).toBeTruthy();
  }));
});
