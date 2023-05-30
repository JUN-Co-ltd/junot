import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchStaffModalComponent } from './search-staff-modal.component';

describe('SearchStaffModalComponent', () => {
  let component: SearchStaffModalComponent;
  let fixture: ComponentFixture<SearchStaffModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SearchStaffModalComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchStaffModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
