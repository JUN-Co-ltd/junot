import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OrderStatusListComponent } from './order-status-list.component';

describe('OrderStatusListComponent', () => {
  let component: OrderStatusListComponent;
  let fixture: ComponentFixture<OrderStatusListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [OrderStatusListComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OrderStatusListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
