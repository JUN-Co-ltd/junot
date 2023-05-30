import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchKojmstModalComponent } from './search-kojmst-modal.component';

describe('SearchKojmstModalComponent', () => {
  let component: SearchKojmstModalComponent;
  let fixture: ComponentFixture<SearchKojmstModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchKojmstModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchKojmstModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
