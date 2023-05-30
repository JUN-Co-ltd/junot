import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FukukitaruOrder01HangTagComponent } from './fukukitaru-order01-hang-tag.component';

describe('FukukitaruOrder01HangTagComponent', () => {
  let component: FukukitaruOrder01HangTagComponent;
  let fixture: ComponentFixture<FukukitaruOrder01HangTagComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FukukitaruOrder01HangTagComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FukukitaruOrder01HangTagComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
