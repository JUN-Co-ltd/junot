import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgForm } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, from, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { SearchTextType, SupplierType, recKbnType, RecKbnDictionary } from 'src/app/const/const';
import { JunpcSirmst } from 'src/app/model/junpc-sirmst';
import { JunpcSirmstSearchCondition } from 'src/app/model/junpc-sirmst-search-condition';
import { SearchSupplierModalComponent } from '../../../search-supplier-modal/search-supplier-modal.component';
import { LoadingService } from '../../../../service/loading.service';
import { GenericList } from 'src/app/model/generic-list';
import { MaintSireSearchCondition } from 'src/app/model/maint/maint-sire-search-condition';
import { MaintSireListStoreService } from '../store/maint-sire-list-store.service';
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
import { BrandCodesSearchConditions } from 'src/app/model/brand-codes-search-conditions';
import { BrandCode } from 'src/app/model/brand-code';

interface Kbn {
  type: string;
  label: string;
  selected: boolean;
}

@Component({
  selector: 'app-maint-sire-list-form',
  templateUrl: './maint-sire-list-form.component.html',
  styleUrls: ['./maint-sire-list-form.component.scss']
})
export class MaintSireListFormComponent implements OnInit {

  /** ローディング表示フラグ */
  @Input()
  isLoading: boolean;

  /** 仕入先メーカー名 */
  @Input()
  sireName: string;

  /** 検索ボタン押下イベント */
  @Output()
  private clickSearchButton = new EventEmitter();

  /** 仕入先入力イベント */
  @Output()
  private changeMaker = new EventEmitter();

  /** 区分リスト(画面入力値保持用) */
  reckbns: Kbn[] = [
    {
      type: recKbnType.SIRE,
      label: RecKbnDictionary[recKbnType.SIRE],
      selected: false
    },
    {
      type: recKbnType.KOJO,
      label: RecKbnDictionary[recKbnType.KOJO],
      selected: false
    },
    {
      type: recKbnType.SPOT,
      label: RecKbnDictionary[recKbnType.SPOT],
      selected: false
    }
  ];

  /** 検索条件 */
  private searchCondition$ = this.store.searchConditionSubject.asObservable();

  /** 検索フォーム */
  formCondition: MaintSireSearchCondition;
  /** ブランドリスト */
  brandList: BrandCode[] = [];

  constructor(
    private loadingService: LoadingService,
    private modalService: NgbModal,
    private store: MaintSireListStoreService,
    private junpcCodmstService: JunpcCodmstService
  ) { }

  ngOnInit() {
    this.searchCondition$.subscribe(searchCondition => this.formCondition = searchCondition);

    // Formのチェックボックスに選択状態を設定
    this.setStorageDataToForm(this.formCondition, this.reckbns);
    this.getBrand().subscribe();
  }

  /**
   * ストレージに保存した入力値をFormに設定する.
   * @param searchForm 検索フォーム値
   * @param reckbns 区分リスト
   */
  private setStorageDataToForm(formCondition: MaintSireSearchCondition, reckbns: Kbn[]): void {
    // 区分リストの初期値を設定
    const kbn = this.setKbnSelected(reckbns);
    formCondition.reckbns.forEach(sesssionData => kbn(sesssionData));
  }

  /**
   * 区分リストの選択状態を設定する.
   * @param reckbns 区分リスト
   * @param sesssionData セッションデータ
   */
  private setKbnSelected = (reckbns: Kbn[]) => (sessionData: string): void =>
    reckbns.filter(reckbn => reckbn.type === sessionData).forEach(reckbn => reckbn.selected = true)

  /**
   * ブランドコードリストを取得するAPIコール
   * @retruns ブランドコードリスト
   */
  private getBrand = (): Observable<GenericList<BrandCode>> =>
    this.junpcCodmstService.getBrandCodes(new BrandCodesSearchConditions()).pipe(
      tap(data => this.brandList = data.items)
    )

  /**
   * 仕入先メーカーを検索するモーダルを表示する.
   */
  openSearchSupplierModal(): void {
    const modalRef = this.modalService.open(SearchSupplierModalComponent);
    modalRef.componentInstance.searchCondition = {
      sirkbn: SupplierType.MDF_MAKER,
      searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH,
      searchText: this.formCondition.sireCode
    } as JunpcSirmstSearchCondition;
    modalRef.componentInstance.default = { sire: this.formCondition.sireCode } as JunpcSirmst;

    from(modalRef.result).pipe(
      tap((result: JunpcSirmst) => {
        this.formCondition.sireCode = result.sire;
        this.sireName = result.name;
      }),
      catchError(() => of(null))
    ).subscribe();
  }

  /**
   * 仕入先コード変更時の処理.
   */
  onChangeSupplier(): void {
    if (this.formCondition.sireCode.length !== 5) {
      this.sireName = null;
      return;
    }

    this.changeMaker.emit(this.formCondition.sireCode);
  }

  /**
   * 検索ボタン押下時の処理.
   * @param searchForm 検索フォーム
   */
  onSearch(searchForm: NgForm): void {
    if (searchForm.invalid) {
      return;
    }

    const searchCondition: MaintSireSearchCondition = { ...searchForm.value };
    // 区分
    searchCondition.reckbns = this.selectedKbn();
    searchCondition.unusedCodeFlg = this.formCondition.unusedCodeFlg;
    this.store.searchConditionSubject.next(searchCondition);
    this.clickSearchButton.emit(searchCondition);
  }

  /**
   * 選択中の区分を取得する.
   * @returns 選択中の区分
   */
  private selectedKbn(): string[] {
    return this.reckbns.filter(reckbns => reckbns.selected).map(reckbns => reckbns.type);
  }
}
