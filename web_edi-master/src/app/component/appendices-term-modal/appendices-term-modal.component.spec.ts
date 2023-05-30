import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppendicesTermModalComponent } from './appendices-term-modal.component';

describe('AppendicesTermModalComponent', () => {
  let component: AppendicesTermModalComponent;
  let fixture: ComponentFixture<AppendicesTermModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppendicesTermModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppendicesTermModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
