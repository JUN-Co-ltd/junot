import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintSireListComponent } from './maint-sire-list.component';

describe('MaintSireListComponent', () => {
  let component: MaintSireListComponent;
  let fixture: ComponentFixture<MaintSireListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintSireListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintSireListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
