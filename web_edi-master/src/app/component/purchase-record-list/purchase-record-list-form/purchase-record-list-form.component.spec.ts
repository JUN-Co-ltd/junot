//PRD_0133 #10181 add JFE start
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PurchaseRecordListFormComponent } from './purchase-record-list-form.component';

describe('PurchaseListFormComponent', () => {
  let component: PurchaseRecordListFormComponent;
  let fixture: ComponentFixture<PurchaseRecordListFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PurchaseRecordListFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PurchaseRecordListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
//PRD_0133 #10181 add JFE end
