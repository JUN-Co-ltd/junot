import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MisleadingRepresentationListComponent } from './misleading-representation-list.component';

describe('MisleadingRepresentationListComponent', () => {
  let component: MisleadingRepresentationListComponent;
  let fixture: ComponentFixture<MisleadingRepresentationListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MisleadingRepresentationListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MisleadingRepresentationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
