import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FukukitaruOrder01WashComponent } from './fukukitaru-order01-wash.component';

describe('FukukitaruOrder01WashComponent', () => {
  let component: FukukitaruOrder01WashComponent;
  let fixture: ComponentFixture<FukukitaruOrder01WashComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FukukitaruOrder01WashComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FukukitaruOrder01WashComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
