import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeliveryListViewModalComponent } from './delivery-list-view-modal.component';

describe('ProductionStatusModalComponent', () => {
  let component: DeliveryListViewModalComponent;
  let fixture: ComponentFixture<DeliveryListViewModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DeliveryListViewModalComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeliveryListViewModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
