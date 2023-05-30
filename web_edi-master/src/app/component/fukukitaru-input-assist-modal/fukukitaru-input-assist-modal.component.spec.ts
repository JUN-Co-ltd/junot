import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FukukitaruInputAssistModalComponent } from './fukukitaru-input-assist-modal.component';

describe('FukukitaruInputAssistModalComponent', () => {
  let component: FukukitaruInputAssistModalComponent;
  let fixture: ComponentFixture<FukukitaruInputAssistModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FukukitaruInputAssistModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FukukitaruInputAssistModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
