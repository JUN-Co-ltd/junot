import { Component, OnInit } from '@angular/core';

import { HeaderService } from '../../service/header.service';

@Component({
  selector: 'app-off-times',
  templateUrl: './off-times.component.html',
  styleUrls: ['./off-times.component.scss']
})
export class OffTimesComponent implements OnInit {

  constructor(
    private headerService: HeaderService
  ) { }

  ngOnInit() {
    this.headerService.hide();
  }

}
