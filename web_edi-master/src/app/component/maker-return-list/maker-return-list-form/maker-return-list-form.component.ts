import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgForm } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { from, of } from 'rxjs';
import { catchError, tap, filter } from 'rxjs/operators';
import { SearchTextType, SupplierType, StaffType } from 'src/app/const/const';
import { JunpcSirmst } from 'src/app/model/junpc-sirmst';
import { JunpcSirmstSearchCondition } from 'src/app/model/junpc-sirmst-search-condition';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { SearchSupplierModalComponent } from '../../search-supplier-modal/search-supplier-modal.component';
import { DateUtilsService } from 'src/app/service/bo/date-utils.service';
import { OnOffType } from 'src/app/const/on-off-type';
import { MakerReturnSearchCondition } from 'src/app/model/maker-return-search-condition';
import { MakerReturnListStoreService } from '../store/maker-return-list-store.service';
import { SearchStaffModalComponent } from '../../search-staff-modal/search-staff-modal.component';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { MakerReturnSearchResult } from 'src/app/model/maker-return-search-result';
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';

@Component({
  selector: 'app-maker-return-list-form',
  templateUrl: './maker-return-list-form.component.html',
  styleUrls: ['./maker-return-list-form.component.scss']
})
export class MakerReturnListFormComponent implements OnInit {

  /** ローディング表示フラグ */
  @Input()
  isLoading: boolean;

  /** ディスタリスト */
  @Input()
  stores: JunpcTnpmst[];

  /** メーカー名 */
  @Input()
  supplierName: string;

  /** 担当者名 */
  @Input()
  mdfStaffName: string;

  /** 検索ボタン押下イベント */
  @Output()
  private clickSearchButton = new EventEmitter();

  /** メーカー入力イベント */
  @Output()
  private changeMaker = new EventEmitter();

  /** セレクトボックスの値リスト */
  readonly SELECT_VALUES: { value: OnOffType, name: string }[] = [
    { value: OnOffType.NO_SELECT, name: '' }, { value: OnOffType.OFF, name: '未送信' }, { value: OnOffType.ON, name: '送信済' }
  ];

  /** 検索条件 */
  private searchCondition$ = this.store.searchConditionSubject.asObservable();

  /** 検索フォーム */
  formCondition: MakerReturnSearchCondition;

  constructor(
    private modalService: NgbModal,
    private dateUtils: DateUtilsService,
    private store: MakerReturnListStoreService,
    private junpcCodmstService: JunpcCodmstService,
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
   * メーカーを検索するモーダルを表示する.
   */
  openSearchSupplierModal(): void {
    const modalRef = this.modalService.open(SearchSupplierModalComponent);
    modalRef.componentInstance.searchCondition = {
      sirkbn: SupplierType.MDF_MAKER,
      searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH,
      searchText: this.formCondition.supplierCode
    } as JunpcSirmstSearchCondition;
    modalRef.componentInstance.default = { sire: this.formCondition.supplierCode } as JunpcSirmst;

    from(modalRef.result).pipe(
      tap((result: JunpcSirmst) => {
        this.formCondition.supplierCode = result.sire;
        this.supplierName = result.name;
      }),
      catchError(() => of(null))
    ).subscribe();
  }

  /**
   * 担当者を検索するモーダルを表示する.
   */
  onSearchStaff(): void {
    const modalRef = this.modalService.open(SearchStaffModalComponent);

    modalRef.componentInstance.staffType = StaffType.PRODUCTION;

    modalRef.componentInstance.defaultStaffCode = this.formCondition.mdfStaffCode;
    modalRef.componentInstance.defaultStaffName = this.mdfStaffName;

    from(modalRef.result).pipe(
      filter(result => result != null),
      tap((result: JunpcCodmst) => {
        this.formCondition.mdfStaffCode = result.code1;
        this.mdfStaffName = result.item2;
      }),
      catchError(() => of(null))
    ).subscribe();
  }

  /**
   * メーカーコード変更時の処理.
   */
  onChangeSupplier(): void {
    if (this.formCondition.supplierCode.length !== 5) {
      this.supplierName = null;
      return;
    }

    this.changeMaker.emit(this.formCondition.supplierCode);
  }

  /**
   * 担当者コード変更時の処理。
   * @param input 担当者コード
   */
  onChangeStaff(input: string): void {

    this.mdfStaffName = null;

    if (input == null || input.length !== 6) { return; }

    this.junpcCodmstService.getStaff(input).pipe(
      tap((result: JunpcCodmst) => {
        this.mdfStaffName = result.item2;
      }),
      catchError(() => of(null))
    ).subscribe();
  }

  /**
   * 検索ボタン押下時の処理.
   * @param searchForm 検索フォーム
   */
  onSearch(searchForm: NgForm): void {
    if (searchForm.invalid) {
      return;
    }

    const searchCondition: MakerReturnSearchCondition = { ...searchForm.value };
    this.store.searchConditionSubject.next(searchCondition);
    this.clickSearchButton.emit(searchCondition);
  }
}
