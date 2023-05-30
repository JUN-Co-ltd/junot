import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeliveryHeaderComponent } from './delivery-header.component';

describe('DeliveryHeaderComponent', () => {
  let component: DeliveryHeaderComponent;
  let fixture: ComponentFixture<DeliveryHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeliveryHeaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeliveryHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
