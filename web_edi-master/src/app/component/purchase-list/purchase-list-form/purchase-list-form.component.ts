import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgForm } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { from, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { SearchTextType, SupplierType } from 'src/app/const/const';
// PRD_0021 add SIT start
import { BrandCode } from 'src/app/model/brand-code';
// PRD_0021 add SIT end
import { JunpcSirmst } from 'src/app/model/junpc-sirmst';
import { JunpcSirmstSearchCondition } from 'src/app/model/junpc-sirmst-search-condition';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { PurchaseSearchCondition } from 'src/app/model/purchase-search-condition';
import { SearchSupplierModalComponent } from '../../search-supplier-modal/search-supplier-modal.component';
import { DateUtilsService } from 'src/app/service/bo/date-utils.service';
import { PurchaseListStoreService } from '../store/purchase-list-store.service';
import { OnOffType } from 'src/app/const/on-off-type';

@Component({
  selector: 'app-purchase-list-form',
  templateUrl: './purchase-list-form.component.html',
  styleUrls: ['./purchase-list-form.component.scss']
})
export class PurchaseListFormComponent implements OnInit {
  /** ローディング表示フラグ */
  @Input()
  isLoading: boolean;

  /** ディスタリスト */
  @Input()
  stores: JunpcTnpmst[];

  // PRD_0021 add SIT start
  /** ブランドリスト */
  @Input()
  brands: BrandCode[] = [];
  // PRD_0021 add SIT end

  /** 仕入先メーカー名 */
  @Input()
  mdfMakerName: string;

  /** 検索ボタン押下イベント */
  @Output()
  private clickSearchButton = new EventEmitter();

  /** 生産メーカー入力イベント */
  @Output()
  private changeMaker = new EventEmitter();

  /** 送信のセレクトボックスの値リスト */
  readonly SEND_SELECT_VALUES: { value: OnOffType, name: string }[] = [
    { value: OnOffType.NO_SELECT, name: '' }, { value: OnOffType.OFF, name: '未送信' }, { value: OnOffType.ON, name: '送信済' }
  ];

  /** 仕入のセレクトボックスの値リスト */
  readonly PURCHASE_SELECT_VALUES: { value: OnOffType, name: string }[] = [
    { value: OnOffType.NO_SELECT, name: '' }, { value: OnOffType.OFF, name: '未入荷' }, { value: OnOffType.ON, name: '入荷済' }
  ];

  /** 検索条件 */
  private searchCondition$ = this.store.searchConditionSubject.asObservable();

  /** 検索フォーム */
  formCondition: PurchaseSearchCondition;

  constructor(
    private modalService: NgbModal,
    private dateUtils: DateUtilsService,
    private store: PurchaseListStoreService
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

    const searchCondition: PurchaseSearchCondition = { ...searchForm.value };
    this.store.searchConditionSubject.next(searchCondition);
    this.clickSearchButton.emit(searchCondition);
  }
}
