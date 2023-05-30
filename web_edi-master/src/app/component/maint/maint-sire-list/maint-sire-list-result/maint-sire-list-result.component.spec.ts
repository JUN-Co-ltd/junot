import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintSireListResultComponent } from './maint-sire-list-result.component';

describe('MaintSireListResultComponent', () => {
  let component: MaintSireListResultComponent;
  let fixture: ComponentFixture<MaintSireListResultComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintSireListResultComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintSireListResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
