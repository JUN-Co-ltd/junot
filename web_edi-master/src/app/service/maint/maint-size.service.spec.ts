//PRD_0137 #10669 add start
import { TestBed, inject } from '@angular/core/testing';

import { MaintSizeService } from './maint-size.service';

describe('MaintSizeService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MaintSizeService]
    });
  });

  it('should be created', inject([MaintSizeService], (service: MaintSizeService) => {
    expect(service).toBeTruthy();
  }));
});
//PRD_0137 #10669 add end
