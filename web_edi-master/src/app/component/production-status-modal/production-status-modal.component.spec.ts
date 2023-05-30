import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductionStatusModalComponent } from './production-status-modal.component';

describe('ProductionStatusModalComponent', () => {
  let component: ProductionStatusModalComponent;
  let fixture: ComponentFixture<ProductionStatusModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProductionStatusModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductionStatusModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
