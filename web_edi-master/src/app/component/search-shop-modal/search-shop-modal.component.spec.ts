import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchShopModalComponent } from './search-shop-modal.component';

describe('SearchShopModalComponent', () => {
  let component: SearchShopModalComponent;
  let fixture: ComponentFixture<SearchShopModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchShopModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchShopModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
