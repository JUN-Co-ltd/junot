import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AttentionModalComponent } from './attention-modal.component';

describe('AttentionModalComponent', () => {
  let component: AttentionModalComponent;
  let fixture: ComponentFixture<AttentionModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AttentionModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AttentionModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
