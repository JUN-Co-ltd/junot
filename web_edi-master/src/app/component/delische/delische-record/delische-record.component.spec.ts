import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DelischeRecordComponent } from './delische-record.component';

describe('DelischeRecordComponent', () => {
  let component: DelischeRecordComponent;
  let fixture: ComponentFixture<DelischeRecordComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DelischeRecordComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DelischeRecordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
