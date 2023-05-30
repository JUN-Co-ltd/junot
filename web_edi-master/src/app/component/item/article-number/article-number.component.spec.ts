import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ArticleNumberComponent } from './article-number.component';

describe('ArticleNumberComponent', () => {
  let component: ArticleNumberComponent;
  let fixture: ComponentFixture<ArticleNumberComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ArticleNumberComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ArticleNumberComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
