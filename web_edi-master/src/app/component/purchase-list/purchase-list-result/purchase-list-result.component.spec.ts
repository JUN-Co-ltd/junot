import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PurchaseListResultComponent } from './purchase-list-result.component';

describe('PurchaseListResultComponent', () => {
  let component: PurchaseListResultComponent;
  let fixture: ComponentFixture<PurchaseListResultComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PurchaseListResultComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PurchaseListResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
