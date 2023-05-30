import { Component, OnInit } from '@angular/core';

import { HeaderService } from '../../service/header.service';

@Component({
  selector: 'app-top',
  templateUrl: './top.component.html',
  styleUrls: ['./top.component.scss']
})
export class TopComponent implements OnInit {

  constructor(
    private headerService: HeaderService,
  ) { }

  ngOnInit() {
    this.headerService.show();
  }

}
