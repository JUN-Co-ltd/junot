import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchCompanyModalComponent } from './search-company-modal.component';

describe('SearchCompanyModalComponent', () => {
  let component: SearchCompanyModalComponent;
  let fixture: ComponentFixture<SearchCompanyModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchCompanyModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchCompanyModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
