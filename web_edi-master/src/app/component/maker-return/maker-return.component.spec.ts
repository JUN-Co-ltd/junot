import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MakerReturnComponent } from './maker-return.component';

describe('MakerReturnComponent', () => {
  let component: MakerReturnComponent;
  let fixture: ComponentFixture<MakerReturnComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MakerReturnComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MakerReturnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
