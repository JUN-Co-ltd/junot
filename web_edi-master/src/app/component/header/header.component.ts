import { Component, OnInit } from '@angular/core';

import { HeaderService } from '../../service/header.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  constructor(
    public headerService: HeaderService,
  ) { }

  ngOnInit() {
    this.headerService.show();
    console.debug('header:' + this.headerService.isDisplay);
  }
}
