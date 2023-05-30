import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MakerReturnListComponent } from './maker-return-list.component';

describe('MakerReturnListComponent', () => {
  let component: MakerReturnListComponent;
  let fixture: ComponentFixture<MakerReturnListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MakerReturnListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MakerReturnListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
