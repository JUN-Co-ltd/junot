//PRD_0133 #10181 add JFE start
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PurchaseRecordListResultComponent } from './purchase-record-list-result.component';

describe('PurchaseListResultComponent', () => {
  let component: PurchaseRecordListResultComponent;
  let fixture: ComponentFixture<PurchaseRecordListResultComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PurchaseRecordListResultComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PurchaseRecordListResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
//PRD_0133 #10181 add JFE end
