import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MakerReturnListFormComponent } from './maker-return-list-form.component';

describe('MakerReturnListFormComponent', () => {
  let component: MakerReturnListFormComponent;
  let fixture: ComponentFixture<MakerReturnListFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MakerReturnListFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MakerReturnListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
