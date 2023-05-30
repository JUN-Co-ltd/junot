import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BulkRegistItemComponent } from './bulk-regist-item.component';

describe('BulkRegistItemComponent', () => {
  let component: BulkRegistItemComponent;
  let fixture: ComponentFixture<BulkRegistItemComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BulkRegistItemComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BulkRegistItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
