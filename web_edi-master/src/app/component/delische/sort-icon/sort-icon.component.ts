import { Component, OnInit, Input } from '@angular/core';
import { OrderByType } from '../../../const/const';

@Component({
  selector: 'app-sort-icon',
  templateUrl: './sort-icon.component.html',
  styleUrls: ['./sort-icon.component.scss']
})
export class SortIconComponent implements OnInit {
  @Input() columnType = '';
  @Input() sorting = { column: '', sort: '' };  // ソート中カラム
  readonly ORDER_TYPE = OrderByType;

  constructor() { }

  ngOnInit() {
  }
}
