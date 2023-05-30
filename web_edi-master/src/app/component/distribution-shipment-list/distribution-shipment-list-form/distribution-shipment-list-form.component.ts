import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgForm } from '@angular/forms';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { DateUtilsService } from 'src/app/service/bo/date-utils.service';
import { DistributionShipmentSearchCondition } from 'src/app/model/distribution-shipment-search-condition';
import { DistributionShipmentListStoreService } from '../store/distribution-shipment-list-store.service';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { BrandCode } from 'src/app/model/brand-code';
import { StringUtilsService } from 'src/app/service/bo/string-utils.service';

@Component({
  selector: 'app-distribution-shipment-list-form',
  templateUrl: './distribution-shipment-list-form.component.html',
  styleUrls: ['./distribution-shipment-list-form.component.scss']
})
export class DistributionShipmentListFormComponent implements OnInit {

  /** ローディング表示フラグ */
  @Input()
  isLoading: boolean;

  /** ディスタリスト */
  @Input()
  stores: JunpcTnpmst[];

  /** ブランドリスト */
  @Input()
  brands: BrandCode[] = [];

  /** アイテムリスト */
  @Input()
  items: JunpcCodmst[] = [];

  /** 事業部リスト */
  @Input()
  departments: JunpcCodmst[] = [];

  /** 配分課名 */
  @Input()
  divisionName: string;

  /** 検索ボタン押下イベント */
  @Output()
  private clickSearchButton = new EventEmitter();

  /** メーカー入力イベント */
  @Output()
  private changeDivision = new EventEmitter();

  /** 検索条件 */
  private searchCondition$ = this.store.searchConditionSubject.asObservable();

  /** 検索フォーム */
  formCondition: DistributionShipmentSearchCondition;

  constructor(
    private dateUtils: DateUtilsService,
    private store: DistributionShipmentListStoreService,
    private stringUtils: StringUtilsService
  ) { }

  ngOnInit() {
    this.searchCondition$.subscribe(
      searchCondition => this.formCondition = searchCondition);
    this.onChangeDivision();
  }

  /**
   * 日付フォーカスアウト時の処理.
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string): void {
    const ngbDate = this.dateUtils.onBlurDate(maskedValue);
    if (ngbDate) { this.formCondition[type] = ngbDate; }
  }

  /**
   * 課コード変更時の処理.
   */
  onChangeDivision(): void {
    // 最大長まで入力されていない場合は検索しない
    if (this.stringUtils.isEmpty(this.formCondition.divisionCode)
    || this.formCondition.divisionCode.length !== 2) {
      this.divisionName = null;
      return;
    }

    this.changeDivision.emit(this.formCondition.divisionCode);
  }

  /**
   * 検索ボタン押下時の処理.
   * @param searchForm 検索フォーム
   */
  onSearch(searchForm: NgForm): void {
    if (searchForm.invalid) {
      return;
    }

    const searchCondition: DistributionShipmentSearchCondition
      = { ...searchForm.value };
    this.store.searchConditionSubject.next(searchCondition);
    this.clickSearchButton.emit(searchCondition);
  }
}
