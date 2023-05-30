//PRD_0133 #10181 add JFE start
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PurchaseRecordListComponent } from './purchase-record-list.component';

describe('PurchaseRecordListComponent', () => {
  let component: PurchaseRecordListComponent;
  let fixture: ComponentFixture<PurchaseRecordListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PurchaseRecordListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PurchaseRecordListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
//PRD_0133 #10181 add JFE end
