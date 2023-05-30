import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintUserComponent } from './maint-user.component';

describe('MaintUserComponent', () => {
  let component: MaintUserComponent;
  let fixture: ComponentFixture<MaintUserComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintUserComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintUserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
