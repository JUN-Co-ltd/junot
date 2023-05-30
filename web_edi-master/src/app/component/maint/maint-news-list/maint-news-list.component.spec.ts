import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintNewsListComponent } from './maint-news-list.component';

describe('MaintNewsListComponent', () => {
  let component: MaintNewsListComponent;
  let fixture: ComponentFixture<MaintNewsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintNewsListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintNewsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
