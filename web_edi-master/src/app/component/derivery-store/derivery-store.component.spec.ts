import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeriveryStoreComponent } from './derivery-store.component';

describe('DeriveryStoreComponent', () => {
  let component: DeriveryStoreComponent;
  let fixture: ComponentFixture<DeriveryStoreComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeriveryStoreComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeriveryStoreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
