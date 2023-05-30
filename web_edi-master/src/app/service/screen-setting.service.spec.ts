import { TestBed, inject } from '@angular/core/testing';

import { ScreenSettingService } from './screen-setting.service';

describe('ScreenSettingService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ScreenSettingService]
    });
  });

  it('should be created', inject([ScreenSettingService], (service: ScreenSettingService) => {
    expect(service).toBeTruthy();
  }));
});
