import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchSupplierModalComponent } from './search-supplier-modal.component';

describe('SearchSupplierModalComponent', () => {
  let component: SearchSupplierModalComponent;
  let fixture: ComponentFixture<SearchSupplierModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchSupplierModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchSupplierModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
