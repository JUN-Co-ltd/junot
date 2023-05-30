import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MakerReturnListResultComponent } from './maker-return-list-result.component';

describe('MakerReturnListResultComponent', () => {
  let component: MakerReturnListResultComponent;
  let fixture: ComponentFixture<MakerReturnListResultComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MakerReturnListResultComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MakerReturnListResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
