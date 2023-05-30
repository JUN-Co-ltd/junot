//PRD_0133 #10181 add JFE start
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgForm ,FormControl} from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { from, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { SearchTextType, SupplierType } from 'src/app/const/const';
import { BrandCode } from 'src/app/model/brand-code';
import { JunpcSirmst } from 'src/app/model/junpc-sirmst';
import { JunpcSirmstSearchCondition } from 'src/app/model/junpc-sirmst-search-condition';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { PurchaseRecordSearchCondition } from 'src/app/model/purchase-record-search-condition';
import { SearchSupplierModalComponent } from '../../search-supplier-modal/search-supplier-modal.component';
import { DateUtilsService } from 'src/app/service/bo/date-utils.service';
import { PurchaseRecordListStoreService } from '../store/purchase-record-list-store.service';
import { PurchaseRecordType } from 'src/app/const/purchase-record-type';

@Component({
  selector: 'app-purchase-record-list-form',
  templateUrl: './purchase-record-list-form.component.html',
  styleUrls: ['./purchase-record-list-form.component.scss']
})
export class PurchaseRecordListFormComponent implements OnInit {
  /** ローディング表示フラグ */
  @Input()
  isLoading: boolean;

  /** ディスタリスト */
  @Input()
  stores: JunpcTnpmst[];

  /** ブランドリスト */
  @Input()
  brands: BrandCode[] = [];

  /** 仕入先メーカー名 */
  @Input()
  mdfMakerName: string;

  /** 仕入先コード */
  @Input()
  sirCodes: string = '';

  /** 事業部マスタリスト */
  @Input()
  divisionMasterList: { id: string, value: string }[] = [];

  /** 数量合計 */
  @Input()
  public fixArrivalCountSum: number;

  /** ｍ級合計 */
  @Input()
  public mKyuSum: number;

  /** 金額合計 */
  @Input()
  public unitPriceSumTotal: number;

  /** 検索ボタン押下イベント */
  @Output()
  private clickSearchButton = new EventEmitter();

  /** 生産メーカー入力イベント */
  @Output()
  private changeMaker = new EventEmitter();

  /** 仕入のセレクトボックスの値リスト */
  readonly PURCHASE_SELECT_VALUES: { value: number, name: string }[] = [
    { value: 0, name: '' },
    { value: 1, name: '仕入' },
    { value: 2, name: '返品' },
    { value: 3, name: '店舗発注店舗' },
    { value: 4, name: '消化委託店舗' }
  ];

  /** 検索条件 */
  private searchCondition$ = this.store.searchConditionSubject.asObservable();

  /** 検索フォーム */
  formCondition: PurchaseRecordSearchCondition;

  constructor(
    private modalService: NgbModal,
    private dateUtils: DateUtilsService,
    private store: PurchaseRecordListStoreService
  ) { }

  ngOnInit() {
    this.searchCondition$.subscribe(searchCondition => this.formCondition = searchCondition);
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
   * 仕入れ先を検索するモーダルを表示する.
   * @param mdfMakerCode フォーム入力値
   */
  openSearchSupplierModal(mdfMakerCode: string): void {
    const modalRef = this.modalService.open(SearchSupplierModalComponent);

    modalRef.componentInstance.searchCondition = {
      sirkbn: SupplierType.MDF_MAKER,
      searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH,
      searchText: mdfMakerCode
    } as JunpcSirmstSearchCondition;
    modalRef.componentInstance.default = { sire: mdfMakerCode } as JunpcSirmst;

    // モーダルからの値を設定する。
    from(modalRef.result).pipe(
      tap((result: JunpcSirmst) => {
        this.formCondition.mdfMakerCode = result.sire;
        this.mdfMakerName = result.name;
      }),
      catchError(() => of(null))
    ).subscribe();
  }

  /**
 * 仕入先コードをテキストボックスに反映する.
 *@param mdfMakerCode フォーム入力値
 */
  onClickReflect(mdfMakerCode: string): void {
    if (this.formCondition.sirCodes == '' || this.formCondition.sirCodes === undefined) {
      this.formCondition.sirCodes = mdfMakerCode;
    }
    else {
      this.formCondition.sirCodes = this.formCondition.sirCodes + ' ' + mdfMakerCode;
    }
  }

  /**
   * メーカーコード変更時の処理.
   * @param mdfMakerCode フォームに入力されたメーカーコード
   */
  onChangeMaker(mdfMakerCode: string): void {
    // 最大長まで入力されていない場合は検索しない
    if (mdfMakerCode.length !== 5) {
      this.mdfMakerName = null;
      return;
    }
    this.changeMaker.emit(mdfMakerCode);
  }

  /**
   * 検索ボタン押下時の処理.
   * @param searchForm 検索フォーム
   */
  onSearch(searchForm: NgForm): void {
    if (searchForm.invalid) {
      return;
    }

    const searchCondition: PurchaseRecordSearchCondition = { ...searchForm.value };
    this.store.searchConditionSubject.next(searchCondition);
    this.clickSearchButton.emit(searchCondition);
  }
}
//PRD_0133 #10181 add JFE end
