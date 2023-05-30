import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FukukitaruMasterTextInputComponent } from './fukukitaru-master-text-input.component';

describe('FukukitaruMasterTextInputComponent', () => {
  let component: FukukitaruMasterTextInputComponent;
  let fixture: ComponentFixture<FukukitaruMasterTextInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [FukukitaruMasterTextInputComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FukukitaruMasterTextInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
