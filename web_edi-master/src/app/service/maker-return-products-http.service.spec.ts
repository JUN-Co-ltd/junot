import { TestBed, inject } from '@angular/core/testing';

import { MakerReturnProductsHttpService } from './maker-return-products-http.service';

describe('MakerReturnProductsHttpService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MakerReturnProductsHttpService]
    });
  });

  it('should be created', inject([MakerReturnProductsHttpService], (service: MakerReturnProductsHttpService) => {
    expect(service).toBeTruthy();
  }));
});
