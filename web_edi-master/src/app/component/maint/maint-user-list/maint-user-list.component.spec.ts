import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintUserListComponent } from './maint-user-list.component';

describe('MaintUserListComponent', () => {
  let component: MaintUserListComponent;
  let fixture: ComponentFixture<MaintUserListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintUserListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintUserListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
