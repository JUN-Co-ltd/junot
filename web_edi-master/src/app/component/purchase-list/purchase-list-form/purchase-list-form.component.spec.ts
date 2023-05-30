import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PurchaseListFormComponent } from './purchase-list-form.component';

describe('PurchaseListFormComponent', () => {
  let component: PurchaseListFormComponent;
  let fixture: ComponentFixture<PurchaseListFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PurchaseListFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PurchaseListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
