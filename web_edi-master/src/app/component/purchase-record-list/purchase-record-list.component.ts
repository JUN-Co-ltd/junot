//PRD_0133 #10181 add JFE start
import { Component, Input, OnInit, Output, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subscription, forkJoin, of } from 'rxjs';
import { map, tap, flatMap, catchError, finalize, filter, first } from 'rxjs/operators';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { BrandCode } from 'src/app/model/brand-code';
import { GenericList } from 'src/app/model/generic-list';
import { JunpcSirmst } from 'src/app/model/junpc-sirmst';
import { JunpcSirmstSearchCondition } from 'src/app/model/junpc-sirmst-search-condition';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { JunpcTnpmstSearchCondition } from 'src/app/model/junpc-tnpmst-search-condition';
import { Session } from 'src/app/model/session';
import { PurchaseRecordSearchCondition } from 'src/app/model/purchase-record-search-condition';
import { PurchaseRecordSearchResult } from 'src/app/model/purchase-record-search-result';
import { PurchaseRecordCsv } from 'src/app/model/purchase-record-csv';
import { HeaderService } from 'src/app/service/header.service';
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
import { JunpcSirmstService } from 'src/app/service/junpc-sirmst.service';
import { JunpcTnpmstHttpService } from 'src/app/service/junpc-tnpmst-http.service';
import { LoadingService } from 'src/app/service/loading.service';
import { LocalStorageService } from 'src/app/service/local-storage.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { PurchaseRecordHttpService } from 'src/app/service/purchase-record-http.service';
import { SessionService } from 'src/app/service/session.service';
import { PurchaseRecordListStoreService } from './store/purchase-record-list-store.service';

import { DateUtilsService } from 'src/app/service/bo/date-utils.service';
import { ListUtilsService } from 'src/app/service/bo/list-utils.service';
import { StringUtilsService } from 'src/app/service/bo/string-utils.service';

import { SearchTextType, Path, SupplierType, Const } from 'src/app/const/const';
import { StorageKey } from 'src/app/const/storage-key';

import { StringUtils } from 'src/app/util/string-utils';
import { CompareResult } from 'src/app/enum/compare-result.enum';
import { FormUtils } from 'src/app/util/form-utils';
import * as iconv from 'iconv-lite';
import { DateUtils } from 'src/app/util/date-utils';
import { NO_01_COLORS } from 'src/app/service/bo/mocks/sku.service.mock';
import { DocumentFileService } from 'src/app/service/document-file.service';
import { FileUtils } from 'src/app/util/file-utils';
import { FileService } from 'src/app/service/file.service';
import { ShopKind } from 'src/app/const/shop-kind';

@Component({
  selector: 'app-purchase-record-list',
  templateUrl: './purchase-record-list.component.html',
  styleUrls: ['./purchase-record-list.component.scss'],
  providers: [PurchaseRecordListStoreService]
})
export class PurchaseRecordListComponent implements OnInit, OnDestroy {

  /** ログインユーザーのセッション */
  private session: Session;

  /** ディスタリスト. */
  stores: JunpcTnpmst[];

  /** ブランドリスト */
  brands: BrandCode[] = [];

  /** 事業部マスタリスト */
  divisionMasterList: { id: string, value: string }[] = [];

  /** 数量合計 */
  public fixArrivalCountSum: number;

  /** ｍ級合計 */
  public mKyuSum: number;

  /** 金額合計 */
  public unitPriceSumTotal: number;

  /**  ファイルダウンロードエラーメッセージコード. */
  public fileDLErrorMessageCode: string;

  /** 仕入先メーカー名 */
  mdfMakerName: String;

  /** 次のページのトークン */
  private nextPageToken = '';

  /** 画面を表示する */
  visibled = false;

  /** ローディング表示フラグ */
  isLoading: boolean;

  /** 検索結果 */
  noResult = true;

  /** ローディング用のサブスクリプション */
  private loadingSubscription: Subscription;

  /** 画面に表示するメッセージ */
  message = {
    /** Body */
    body: {
      /** 異常系 */
      error: { code: '', param: null }
    },
    /** フッター */
    footer: {
      /** 正常系 */
      success: { code: '', param: null }
    }
  };

  constructor(
    private router: Router,
    private purchaseRecordHttpService: PurchaseRecordHttpService,
    private messageConfirmModalService: MessageConfirmModalService,
    private headerService: HeaderService,
    private sessionService: SessionService,
    private junpcCodmstService: JunpcCodmstService,
    private junpcTnpmstService: JunpcTnpmstHttpService,
    private junpcSirmstService: JunpcSirmstService,
    private dateUtilsService: DateUtilsService,
    private localStorageService: LocalStorageService,
    private loadingService: LoadingService,
    private listUtils: ListUtilsService,
    private stringUtils: StringUtilsService,
    private store: PurchaseRecordListStoreService,
    private fileService: FileService
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.session = this.sessionService.getSaveSession();

    this.loadingService.clear();
    this.loadingSubscription = this.loadingService.isLoading$.subscribe(isLoading => this.isLoading = isLoading);
    let loadingToken = null;
    this.fileDLErrorMessageCode = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      flatMap(() => this.getDivisionList()),
      flatMap(() => this.fetchStores()),
      map(stores => this.generateInitialFormValue(this.session, stores)),
      tap(() => this.visibled = true),
      catchError(this.showErrorModal),
      finalize(() => this.loadingService.stop(loadingToken)),
    ).subscribe();
  }

  ngOnDestroy(): void {
    this.loadingSubscription.unsubscribe();
  }

  //PRD_0190 JFE mod start
  /**
   * @returns ディスタリスト
   */
    // private fetchStores(): Observable<JunpcTnpmst[]> {
    //   return this.junpcTnpmstService.search({ shpcdAhead: Const.PURCHASE_SHPCD_PREFIX } as JunpcTnpmstSearchCondition).pipe(
    //     map(res => this.stores = res.items)
    //   );
    // }
  private fetchStores(): Observable<JunpcTnpmst[]> {
      /* PRD_0007 mod SIT start */
      //return this.junpcTnpmstService.search({ shpcdAhead: Const.MAKER_RETURN_SHPCD_PREFIX } as JunpcTnpmstSearchCondition).pipe(
      //  map(res => this.stores = res.items)
      return this.junpcTnpmstService.search({
        shpcdAhead: Const.MAKER_RETURN_SHPCD_PREFIX + ' ' + Const.PURCHASE_SHPCD_PREFIX,
        shopkind: ShopKind.WARE_HOUSE as ShopKind
      } as JunpcTnpmstSearchCondition).pipe(
      map(res => this.stores = res.items)
      /* PRD_0007 mod SIT end */
    );
  }
  //PRD_0190 JFE mod end
  /**
  * 事業部リスト
  * @returns Observable<any>
  */
  private getDivisionList(): Observable<any> {
    return this.junpcCodmstService.getAllDivisions().pipe(
      tap(result =>
        this.divisionMasterList = result.items.map(item => ({ id: item.code1, value: item.item2 })))
    );
  }

  /**
   * @param session セッション
   * @param stores ディスタリスト
   * @returns 検索フォーム初期値
   */
  private generateInitialFormValue(session: Session, stores: JunpcTnpmst[]): PurchaseRecordSearchCondition {
    // storageから前回入力した検索条件を取得する。
    const storageCondition: PurchaseRecordSearchCondition = JSON.parse(
      this.localStorageService.getSaveLocalStorage(session, StorageKey.PURCHASE_RECORD_LIST_SEARCH_CONDITION));

    const formCondition = storageCondition == null ? new PurchaseRecordSearchCondition() : storageCondition;

    const dayToday = this.dateUtilsService.generateCurrentDate();
    // 納品日fromは当日日付
    const searchCondition = Object.assign({ ...formCondition }, { recordAtFrom: dayToday });
    // 納品日toの値が当日より前の場合、当日を設定
    searchCondition.recordAtTo =
      (FormUtils.isEmpty(storageCondition) || FormUtils.isEmpty(storageCondition.recordAtTo)) ? dayToday :
        this.dateUtilsService.compare(searchCondition.recordAtTo as NgbDateStruct, dayToday) == CompareResult.Less
          ? dayToday : searchCondition.recordAtTo;
    //PRD_0174 #10181　JFE del start --初回起動時の初期選択は空欄にするため、削除
    // ディスタの初期値はマスタの先頭
    // searchCondition.arrivalShop = this.stringUtils.isEmpty(searchCondition.arrivalShop) ?
    //   stores[0].shpcd : searchCondition.arrivalShop;
    //PRD_0174 #10181　JFE del end
    // 生産メーカー名取得
    const mdfMakerCode = searchCondition.mdfMakerCode;
    if (this.stringUtils.isNotEmpty(mdfMakerCode) && mdfMakerCode.length === 5) {
      this.onChangeMaker(mdfMakerCode);
    }

    if (FormUtils.isEmpty(searchCondition.purchaseType)) {
      searchCondition.purchaseType = 0;
    }
    this.store.searchConditionSubject.next(searchCondition);
    return searchCondition;
  }

  /**
   * 生産メーカーフォーム内容変更時イベント
   * @param mdfMakerCode 生産メーカーフォーム内容
   */
  onChangeMaker(mdfMakerCode: String): void {
    this.mdfMakerName = '';

    this.junpcSirmstService.getSirmst({
      sirkbn: SupplierType.MDF_MAKER,
      searchType: SearchTextType.CODE_PARTIAL_MATCH,
      searchText: mdfMakerCode
    } as JunpcSirmstSearchCondition).pipe(
      tap(this.extractMakerName)
    ).subscribe();
  }


  /**
   * 仕入マスタ検索結果からメーカー名を設定する.
   * @param result 仕入マスタ検索結果
   * @retrun メーカー名
   */
  private extractMakerName = (result: GenericList<JunpcSirmst>): string =>
    this.mdfMakerName = result == null || this.listUtils.isEmpty(result.items) ?
      '' : result.items[0].name

  /**
   * 検索ボタン押下時イベント.
   * @param searchForm 検索フォーム値
   */
  onClickSearchBtn(searchForm: PurchaseRecordSearchCondition): void {
    this.noResult = true;
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      tap(this.clearMessage),
      tap(() => this.saveSearchConditions(this.session, searchForm)), // 検索条件保持
      tap(() => this.store.purchaseSearchResultSubject.next([])),  // 既存の検索結果クリア
      flatMap(() => this.searchPurchase(searchForm)),  // 検索処理
      catchError(this.showErrorModal),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();

  }

  /**
   * 仕入一覧検索処理.
   * @param searchFormValue 検索フォーム値
   * @returns 検索結果
   */
  private searchPurchase(searchFormValue: PurchaseRecordSearchCondition): Observable<PurchaseRecordSearchResult[]> {
    //検索項目「品番」でハイフンが使われていた場合を考慮して置換する。
    if (this.stringUtils.isNotEmpty(searchFormValue.partNo)) {
      if (searchFormValue.partNo.length >= 0) {
        searchFormValue.partNo = searchFormValue.partNo.replace(/-/g, '');
      }
    }
    //PRD_0185 #10181　JFE add start
    if (this.stringUtils.isEmpty(searchFormValue.arrivalShop)) {
      searchFormValue.arrivalShop = "";
    }
    //PRD_0185 #10181　JFE add end
    this.fileDLErrorMessageCode = null;
    return this.purchaseRecordHttpService.search(searchFormValue).pipe(
      tap(result => { if (result.items.length !== 0) { this.noResult = false } }), //検索結果があれば、PDF/CSV作成ボタンを有効化する。
      tap(result => this.nextPageToken = result.nextPageToken),
      tap(result => this.setSearchResultMessage(result.items)),
      flatMap(result => forkJoin(this.store.previousSearchResult$, of(result))),
      map(([previous, result]) => previous.concat(result.items)),
      tap(result => this.store.purchaseSearchResultSubject.next(result)),
      tap(result => this.store.createForm(result)),
      tap(result => this.calcTotal(result))
    );
  }

  /**
   * 検索条件をstorageに保持.
   * @param session セッション
   * @param searchCondition 検索条件
   */
  private saveSearchConditions(session: Session, searchCondition: PurchaseRecordSearchCondition): void {
    this.localStorageService.createLocalStorage(session, StorageKey.PURCHASE_RECORD_LIST_SEARCH_CONDITION, searchCondition);
  }

  /**
   * 最下部までスクロール時の処理.
   * 次のページのトークンがある場合、次のリストを取得する.
   */
  onScrollToBottom(): void {
    if (StringUtils.isEmpty(this.nextPageToken)) {
      return;
    }
    const nextPageToken = this.nextPageToken;
    this.nextPageToken = null;

    this.searchPurchase({ pageToken: nextPageToken } as PurchaseRecordSearchCondition).subscribe();

  }


  /**
   * エラーモーダルを表示する.
   * @param error エラー情報
   * @returns エラーモーダルの結果
   * - true : 「OK」が押された場合
   * - false : モーダルが閉じられた場合
   */
  private showErrorModal = (error: any): Observable<boolean> => this.messageConfirmModalService.openErrorModal(error);

  /**
   * 検索結果に応じたメッセージをセットする.
   * @param results 検索結果
   */
  private setSearchResultMessage(results: PurchaseRecordSearchResult[]): void {
    if (results.length === 0) {
      this.message.body.error = { code: 'INFO.RESULT_ZERO', param: null };
    }
  }


  /**
   * メッセージをクリアする.
   */
  private clearMessage = (): void => {
    this.message = {
      body: {
        error: { code: '', param: null }
      },
      footer: {
        success: { code: '', param: null }
      }
    };
  }

  /**
   * 検索結果の合計を計算する.
   * @param purchasesRecord 仕入実績情報リスト
   */
  private calcTotal(purchasesRecord: PurchaseRecordSearchResult[]): void {
    let sumFixArrival = 0;
    let sumMkyu = 0;
    let sumUnitPrice = 0;

    for (let p of purchasesRecord) {
      // 数量加算
      if (p.fixArrivalCountSum === undefined) {
        sumFixArrival += 0;
      } else {
        sumFixArrival += Number(p.fixArrivalCountSum);
      }

      //m級加算
      if (p.mkyuSum === undefined) {
        sumMkyu += 0;
      } else {
        sumMkyu += Number(p.mkyuSum);
      }
      //金額加算
      if (p.unitPriceSumTotal === undefined) {
        sumUnitPrice += 0;
      } else {
        sumUnitPrice += Number(p.unitPriceSumTotal);
      }
      //複数回加算されるのを避ける
      if (sumFixArrival !== 0 || sumMkyu !== 0 || sumUnitPrice !== 0) {
        break;
      }
    }

    this.fixArrivalCountSum = sumFixArrival;
    this.mKyuSum = sumMkyu;
    this.unitPriceSumTotal = sumUnitPrice;

  }

  /**
* PDF作成ボタン押下処理.
*/
  onCreatePDF(): void {
    const storageCondition: PurchaseRecordSearchCondition = JSON.parse(
      this.localStorageService.getSaveLocalStorage(this.session, StorageKey.PURCHASE_RECORD_LIST_SEARCH_CONDITION));

    const formCondition = storageCondition == null ? new PurchaseRecordSearchCondition() : storageCondition;

    this.purchaseRecordHttpService.createPdf(formCondition).subscribe(
      resultPDF =>{
        const data = this.fileService.splitBlobAndFileName(resultPDF);
        FileUtils.downloadFile(data.blob, data.fileName);
      }, );
  }

  /**
  * CSV作成ボタン押下処理.
  */
  onCreateCSV(): void {
    const storageCondition: PurchaseRecordSearchCondition = JSON.parse(
      this.localStorageService.getSaveLocalStorage(this.session, StorageKey.PURCHASE_RECORD_LIST_SEARCH_CONDITION));

    const formCondition = storageCondition == null ? new PurchaseRecordSearchCondition() : storageCondition;

    this.purchaseRecordHttpService.searchCsv(formCondition).subscribe(
      resultCSV => {
        const blob = new Blob([iconv.encode(this.arrToCSV(resultCSV), 'Shift_JIS')], { 'type': 'text/csv' });
        const blobURL = window.URL.createObjectURL(blob);
        //ファイル名の設定 ：PurchaseRecordyyyyMMddHHmmSS
        var currentDateTime = new Date();
        const currentY = currentDateTime.getFullYear();
        const currentM = currentDateTime.getMonth() + 1;
        var currentD = currentDateTime.getDate().toString();
        //日付が一桁の場合は前ゼロを付ける
        if (currentD.length == 1) {
          currentD = "0" + currentD;
        }
        const currentH = currentDateTime.getHours();
        const currentMin = currentDateTime.getMinutes();
        const currentS = currentDateTime.getSeconds();
        const fileDate = ""+currentY + currentM + currentD + currentH + currentMin + currentS;
        // a要素を作成する
        let ele = document.createElement('a');
        // a要素に出力情報を追加
        ele.setAttribute('download', 'PurchaseRecord' + fileDate + '.csv');
        //a要素に出力データを追加
        ele.setAttribute('href', blobURL);
        ele.style.visibility = 'hidden';
        // HTMLドキュメントにa要素を追加
        document.body.appendChild(ele);
        // HTMLドキュメントに追加したa要素を実行(clickイベント実行)
        ele.click();
        // HTMLドキュメントに追加したa要素を削除
        document.body.removeChild(ele);
      }
    )
  }


  /**
 * 出力データ作成
 * @returns 出力テキスト
 */
  private arrToCSV(result: GenericList<PurchaseRecordCsv>): string {
    let outputData: string[][] = [];
    // ヘッダー作成
    outputData.push(
      ["仕入先コード", "仕入先名称", "入荷場所","計上日", "伝票No", "伝区", "品番", "数量", "m数", "単価", "金額"]
    );

    // // レコード処理
    result.items.forEach(r => {
      outputData.push([r.supplierCode, r.supplierName, r.logisticsCode + r.arrivalPlace, r.recordAt, r.purchaseVoucherNumber, r.purchaseType, r.partNo, r.fixArrivalCount, r.mkyu, r.purchaseUnitPrice, r.unitPriceSum])
    });

    return outputData.map(row => row.join(',')).join('\r\n');
  }
}
//PRD_0133 #10181 add JFE end
