import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintNewsComponent } from './maint-news.component';

describe('MaintNewsComponent', () => {
  let component: MaintNewsComponent;
  let fixture: ComponentFixture<MaintNewsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintNewsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintNewsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
