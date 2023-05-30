import { TestBed, inject } from '@angular/core/testing';

import { MaintNewsService } from './maint-news.service';

describe('MaintNewsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MaintNewsService]
    });
  });

  it('should be created', inject([MaintNewsService], (service: MaintNewsService) => {
    expect(service).toBeTruthy();
  }));
});
