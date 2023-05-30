import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MisleadingApproveComponent } from './misleading-approve.component';

describe('MisleadingApproveComponent', () => {
  let component: MisleadingApproveComponent;
  let fixture: ComponentFixture<MisleadingApproveComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MisleadingApproveComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MisleadingApproveComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
