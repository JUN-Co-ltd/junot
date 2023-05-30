import { TestBed, inject } from '@angular/core/testing';

import { StringUtilsService } from './string-utils.service';

describe('StringUtilsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [StringUtilsService]
    });
  });

  it('should be created', inject([StringUtilsService], (service: StringUtilsService) => {
    expect(service).toBeTruthy();
  }));
});
