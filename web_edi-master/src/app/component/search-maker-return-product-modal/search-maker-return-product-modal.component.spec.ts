import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchMakerReturnProductModalComponent } from './search-maker-return-product-modal.component';

describe('SearchMakerReturnProductModalComponent', () => {
  let component: SearchMakerReturnProductModalComponent;
  let fixture: ComponentFixture<SearchMakerReturnProductModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchMakerReturnProductModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchMakerReturnProductModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
