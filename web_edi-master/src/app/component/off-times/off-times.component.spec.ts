import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OffTimesComponent } from './off-times.component';

describe('OffTimesComponent', () => {
  let component: OffTimesComponent;
  let fixture: ComponentFixture<OffTimesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OffTimesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OffTimesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
