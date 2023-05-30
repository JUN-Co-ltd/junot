import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MisleadingApproveFormComponent } from './misleading-approve-form.component';

describe('MisleadingApproveFormComponent', () => {
  let component: MisleadingApproveFormComponent;
  let fixture: ComponentFixture<MisleadingApproveFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MisleadingApproveFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MisleadingApproveFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
