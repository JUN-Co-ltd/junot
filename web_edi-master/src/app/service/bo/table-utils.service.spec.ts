import { TestBed, inject } from '@angular/core/testing';

import { TableUtilsService } from './table-utils.service';

describe('TableUtilsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TableUtilsService]
    });
  });

  it('should be created', inject([TableUtilsService], (service: TableUtilsService) => {
    expect(service).toBeTruthy();
  }));
});
