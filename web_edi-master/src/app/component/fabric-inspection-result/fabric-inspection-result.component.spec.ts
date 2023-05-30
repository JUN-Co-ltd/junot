import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FabricInspectionResultComponent } from './fabric-inspection-result.component';

describe('FabricInspectionResultComponent', () => {
  let component: FabricInspectionResultComponent;
  let fixture: ComponentFixture<FabricInspectionResultComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FabricInspectionResultComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FabricInspectionResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
