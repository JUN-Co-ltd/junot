import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DelischeComponent } from './delische.component';

describe('DeliScheComponent', () => {
  let component: DelischeComponent;
  let fixture: ComponentFixture<DelischeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DelischeComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DelischeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
