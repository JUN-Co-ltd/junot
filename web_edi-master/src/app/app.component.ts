import { Component, Injectable, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { NgbDatepickerI18n, NgbDateStruct, NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';
import { NgbDateFRParserFormatter } from './lib/ngb-date-fr-parser-formatter';
import { HeaderService } from './service/header.service';
import { LoadingService } from './service/loading.service';

const I18N_VALUES = {
  'jpn': {
    weekdays: ['月', '火', '水', '木', '金', '土', '日'],
    months: ['１', '２', '３', '４', '５', '６', '７', '８', '９', '１０', '１１', '１２'],
  }
};

@Injectable()
export class I18n {
  language = 'jpn';
}

@Injectable()
export class CustomDatepickerI18n extends NgbDatepickerI18n {
  constructor(private _i18n: I18n) {
    super();
  }

  getWeekdayShortName(weekday: number): string {
    return I18N_VALUES[this._i18n.language].weekdays[weekday - 1];
  }
  getMonthShortName(month: number): string {
    return I18N_VALUES[this._i18n.language].months[month - 1];
  }
  getMonthFullName(month: number): string {
    return this.getMonthShortName(month);
  }
  getDayAriaLabel(date: NgbDateStruct): string {
    return `${ date.day }-${ date.month }-${ date.year }`;
  }
}
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  providers: [I18n, {
    provide: NgbDatepickerI18n,
    useClass: CustomDatepickerI18n
  }, { provide: NgbDateParserFormatter, useClass: NgbDateFRParserFormatter }]
})
export class AppComponent implements OnInit {

  constructor(
    public translate: TranslateService,
    public headerService: HeaderService,
    public loadingService: LoadingService) {
    translate.setDefaultLang('ja');
    translate.use('ja');
  }

  /** 初期処理 */
  ngOnInit() { }
}
