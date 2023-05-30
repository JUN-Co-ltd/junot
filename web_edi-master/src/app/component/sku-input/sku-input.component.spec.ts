import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SkuInputComponent } from './sku-input.component';

describe('SkuInputComponent', () => {
  let component: SkuInputComponent;
  let fixture: ComponentFixture<SkuInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SkuInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SkuInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
