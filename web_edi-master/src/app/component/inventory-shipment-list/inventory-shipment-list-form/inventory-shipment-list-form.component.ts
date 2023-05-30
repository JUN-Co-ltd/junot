import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgForm } from '@angular/forms';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { DateUtilsService } from 'src/app/service/bo/date-utils.service';
import { InventoryShipmentSearchCondition } from 'src/app/model/inventory-shipment-search-condition';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { BrandCode } from 'src/app/model/brand-code';
import { InventoryShipmentListStoreService } from '../store/inventory-shipment-list-store.service';
import { InstructorSystemType } from 'src/app/const/const';
import { StringUtilsService } from 'src/app/service/bo/string-utils.service';

@Component({
  selector: 'app-inventory-shipment-list-form',
  templateUrl: './inventory-shipment-list-form.component.html',
  styleUrls: ['./inventory-shipment-list-form.component.scss']
})
export class InventoryShipmentListFormComponent implements OnInit {

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

  /** 指示元のセレクトボックスの値 */
  readonly INSTRUCTION_SYSTEME_SELECT_VALUES: number[] = [
      InstructorSystemType.JADORE,
      InstructorSystemType.ZOZO,
      InstructorSystemType.SV,
      InstructorSystemType.SCS,
      InstructorSystemType.ARO,
    ];

  /** 検索条件 */
  private searchCondition$ = this.store.searchConditionSubject.asObservable();

  /** 検索フォーム */
  formCondition: InventoryShipmentSearchCondition;

  constructor(
    private dateUtils: DateUtilsService,
    private store: InventoryShipmentListStoreService,
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

    const searchCondition: InventoryShipmentSearchCondition
      = { ...searchForm.value };
    this.store.searchConditionSubject.next(searchCondition);
    this.clickSearchButton.emit(searchCondition);
  }
}
