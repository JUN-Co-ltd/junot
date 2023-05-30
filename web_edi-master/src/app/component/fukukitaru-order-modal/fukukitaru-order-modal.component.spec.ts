import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FukukitaruOrderModalComponent } from './fukukitaru-order-modal.component';

describe('FukukitaruOrderModalComponent', () => {
  let component: FukukitaruOrderModalComponent;
  let fixture: ComponentFixture<FukukitaruOrderModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FukukitaruOrderModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FukukitaruOrderModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
