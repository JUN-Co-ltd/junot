import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaterialOrderSubmitConfirmModalComponent } from './material-order-submit-confirm-modal.component';

describe('MaterialOrderSubmitConfirmModalComponent', () => {
  let component: MaterialOrderSubmitConfirmModalComponent;
  let fixture: ComponentFixture<MaterialOrderSubmitConfirmModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaterialOrderSubmitConfirmModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaterialOrderSubmitConfirmModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
