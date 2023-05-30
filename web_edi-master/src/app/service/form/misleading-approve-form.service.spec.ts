import { TestBed, inject } from '@angular/core/testing';

import { MisleadingApproveFormService } from './misleading-approve-form.service';

describe('MisleadingApproveFormService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MisleadingApproveFormService]
    });
  });

  it('should be created', inject([MisleadingApproveFormService], (service: MisleadingApproveFormService) => {
    expect(service).toBeTruthy();
  }));
});
