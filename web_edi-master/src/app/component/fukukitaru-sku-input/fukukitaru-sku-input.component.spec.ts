import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FukukitaruSkuInputComponent } from './fukukitaru-sku-input.component';

describe('FukukitaruSkuInputComponent', () => {
  let component: FukukitaruSkuInputComponent;
  let fixture: ComponentFixture<FukukitaruSkuInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FukukitaruSkuInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FukukitaruSkuInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
