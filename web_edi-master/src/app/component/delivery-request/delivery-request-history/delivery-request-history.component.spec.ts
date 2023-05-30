import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeliveryRequestHistoryComponent } from './delivery-request-history.component';

describe('DeliveryRequestHistoryComponent', () => {
  let component: DeliveryRequestHistoryComponent;
  let fixture: ComponentFixture<DeliveryRequestHistoryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeliveryRequestHistoryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeliveryRequestHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
