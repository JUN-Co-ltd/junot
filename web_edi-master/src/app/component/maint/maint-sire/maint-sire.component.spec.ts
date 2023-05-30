import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintSireComponent } from './maker-return.component';

describe('MaintSireComponent', () => {
  let component: MaintSireComponent;
  let fixture: ComponentFixture<MaintSireComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintSireComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintSireComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
