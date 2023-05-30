import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintTopComponent } from './maint-top.component';

describe('MaintTopComponent', () => {
  let component: MaintTopComponent;
  let fixture: ComponentFixture<MaintTopComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintTopComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintTopComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
