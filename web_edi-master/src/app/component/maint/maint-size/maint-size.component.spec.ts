//PRD_0137 #10669 add start
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintSizeComponent } from './maint-size.component';

describe('MaintSizeComponent', () => {
  let component: MaintSizeComponent;
  let fixture: ComponentFixture<MaintSizeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintSizeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintSizeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
//PRD_0137 #10669 add end
