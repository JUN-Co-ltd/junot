import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintCodeComponent } from './maint-code.component';

describe('MaintCodeComponent', () => {
  let component: MaintCodeComponent;
  let fixture: ComponentFixture<MaintCodeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintCodeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintCodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
