import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintSireListFormComponent } from './maint-sire-list-form.component';

describe('MaintSireListFormComponent', () => {
  let component: MaintSireListFormComponent;
  let fixture: ComponentFixture<MaintSireListFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintSireListFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintSireListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
