import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SkuSelectComponent } from './sku-select.component';

describe('SkuSelectComponent', () => {
  let component: SkuSelectComponent;
  let fixture: ComponentFixture<SkuSelectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SkuSelectComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SkuSelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
