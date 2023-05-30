import { TestBed, inject } from '@angular/core/testing';

import { MessageConfirmModalService } from './message-confirm-modal.service';

describe('MessageConfirmModalService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MessageConfirmModalService]
    });
  });

  it('should be created', inject([MessageConfirmModalService], (service: MessageConfirmModalService) => {
    expect(service).toBeTruthy();
  }));
});
