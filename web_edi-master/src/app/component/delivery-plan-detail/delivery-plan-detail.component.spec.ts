import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeliveryPlanDetailComponent } from './delivery-plan-detail.component';

describe('DeliveryPlanDetailComponent', () => {
  let component: DeliveryPlanDetailComponent;
  let fixture: ComponentFixture<DeliveryPlanDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeliveryPlanDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeliveryPlanDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
