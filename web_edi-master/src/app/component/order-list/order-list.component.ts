import { Component, OnInit, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';

import { NgbTabChangeEvent, NgbTabset } from '@ng-bootstrap/ng-bootstrap';

import { ScrollEvent } from 'ngx-scroll-event';

import { StaffType, OrderKeywordType, OrderApprovalStatus, AuthType, KeywordConditionsLimit, OrderBy } from '../../const/const';
import { CodeMaster } from '../../const/code-master';
import { BusinessCheckUtils } from '../../util/business-check-utils';
import { ExceptionUtils } from '../../util/exception-utils';
import { StringUtils } from '../../util/string-utils';

import { SessionService } from '../../service/session.service';
import { OrderService } from '../../service/order.service';
import { LocalStorageService } from '../../service/local-storage.service';
import { LoadingService } from '../../service/loading.service';

import { VOrder } from '../../model/v-order';
import { OrderListSearchFormConditions, OrderSearchConditions } from '../../model/search-conditions';
import { Session } from '../../model/session';


@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss']
})
export class OrderListComponent implements OnInit {
  @ViewChild('tabSet') ngbTabset: NgbTabset;

  // htmlからの定数値参照
  readonly STAFF_TYPE = StaffType;
  readonly KEYWORD_TYPE = OrderKeywordType;
  readonly AUTH_INTERNAL = AuthType.AUTH_INTERNAL;

  private session: Session; // セッション情報
  affiliation: AuthType; // ログインユーザの情報
  readonly STORAGE_KEY_ORDER_LIST_SEARCH_CONDITIONS = 'orderListSearchFormConditions'; // 検索条件のSessionKey
  readonly STORAGE_KEY_ORDER_LIST_SELECTED_TAB_ID = 'orderListSelectedTabId';  // 選択タブのSessionKey
  orderListSearchFormConditions: OrderListSearchFormConditions = null;  // 発注一覧検索Formの入力値
  nextPageToken: string;                                              // 次のページのトークン

  overall_msg_code = '';  // エラーメッセージ
  seasonMasterList: { id: number, value: string }[] = [];  // シーズンリスト

  submitted = false;  // submit押下フラグ

  allOrderList: VOrder[] = [];                // 全発注情報リスト
  confirmPendingOrderList: VOrder[] = [];     // 受注確定待ちリスト
  mdPendingOrderList: VOrder[] = [];          // 発注承認待ちリスト
  misleadingPendingOrderList: VOrder[] = [];  // 優良誤認待ちリスト
  productionOrderList: VOrder[] = [];         // 生産中リスト
  productCompleteOrderList: VOrder[] = [];    // 完納リスト

  constructor(
    private sessionService: SessionService,
    private localStorageService: LocalStorageService,
    private orderService: OrderService,
    private loadingService: LoadingService
  ) { }

  ngOnInit() {
    this.session = this.sessionService.getSaveSession();
    this.orderListSearchFormConditions = JSON.parse(  // storageから前回入力した検索条件を取得する。
      this.localStorageService.getSaveLocalStorage(this.session, this.STORAGE_KEY_ORDER_LIST_SEARCH_CONDITIONS));
    this.affiliation = this.session.affiliation;

    this.seasonMasterList = CodeMaster.subSeason;    // シーズンマスタ設定

    // storageから検索条件が取得されなかった場合、初期設定する
    if (!this.orderListSearchFormConditions) {
      this.orderListSearchFormConditions = new OrderListSearchFormConditions();
      this.orderListSearchFormConditions.staffName = String(this.session.accountName);
      // PRD_0107 add JFE Start
      this.orderListSearchFormConditions.keywordType = OrderKeywordType.ITEM_NO;
      // PRD_0107 add JFE End

    }
    // 発注検索処理
    this.searchOrder(this.orderListSearchFormConditions);
    this.ngbTabset.activeId = JSON.parse(
      this.localStorageService.getSaveLocalStorage(this.session, this.STORAGE_KEY_ORDER_LIST_SELECTED_TAB_ID));
  }

  /**
   * 検索ボタン押下時の処理.
   * @param orderListSearchFormConditions 発注情報検索API用Model
   * @param searchForm 検索Form
   */
  onClickSearchBtn(orderListSearchFormConditions: OrderListSearchFormConditions, searchForm: NgForm): void {
    this.submitted = true;
    if (searchForm && searchForm.invalid) { return; } // validationエラーがある場合終了
    this.loadingService.loadStart();
    this.searchOrder(orderListSearchFormConditions);
  }

  /**
   * 発注検索処理を行う.
   * @param orderListSearchFormConditions 発注情報検索API用Model
   */
  searchOrder(orderListSearchFormConditions: OrderListSearchFormConditions): void {
    this.allOrderList = [];               // 全発注情報リストをクリア
    this.confirmPendingOrderList = [];    // 受注確定待ちリストをクリア
    this.mdPendingOrderList = [];         // 発注承認待ちリストをクリア
    this.misleadingPendingOrderList = []; // 優良誤認待ちリストをクリア
    this.productionOrderList = [];        // 生産中リストをクリア
    this.productCompleteOrderList = [];   // 完納リストをクリア
    this.overall_msg_code = '';           // エラーメッセージをクリア

    // 入力値をAPI検索用Modelに設定する。
    const orderRequestApiConditions = this.generateApiSearchConditions(orderListSearchFormConditions);

　　// キーワードの個数チェック
    if (StringUtils.isNotEmpty(orderListSearchFormConditions.keyword)) {
      const result = orderListSearchFormConditions.keyword.split(/[\x20\u3000]/);
      if (result.length > KeywordConditionsLimit.ORDER_LIST) {
        this.overall_msg_code = 'INFO.CONDITIONS_LIMIT';
        this.loadingService.loadEnd();
        return;
      }
    }
    // 発注情報検索処理
    this.orderService.getOrderList(orderRequestApiConditions).toPromise().then(
      genericList => {
        this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
        const orderList = genericList.items;
        // console.debug('発注取得:', orderList);
        if (orderList.length <= 0) {  // 検索結果なし
          this.overall_msg_code = 'INFO.RESULT_ZERO';
        } else {
          // 検索結果1以上の場合はリストに設定する。
          this.setOrderList(orderList);
        }
        this.loadingService.loadEnd();
      },
      error => this.handleApiError(error)
    );
  }

  /**
   * 検索Form入力値をAPI検索用Modelに設定して返す.
   * @param orderListSearchFormConditions 検索Form入力値
   * @returns API検索用Model
   */
  private generateApiSearchConditions(orderListSearchFormConditions: OrderListSearchFormConditions): OrderSearchConditions {
    // キーワード、担当者設定
    const keyword = this.setKeywordSearchCondition(orderListSearchFormConditions);
    const staffType = this.setStaffTypeSearchCondition(orderListSearchFormConditions);
    // 検索条件をsessionに保持
    this.localStorageService.createLocalStorage(this.session, this.STORAGE_KEY_ORDER_LIST_SEARCH_CONDITIONS, orderListSearchFormConditions);
    return {
      partNo: keyword.partNo,
      productName: keyword.productName,
      brandCode: keyword.brandCode,
      itemCode: keyword.itemCode,
      maker: keyword.maker,
      orderNumberText: keyword.orderNumberText,

      plannerName: staffType.plannerName,
      mdfStaffName: staffType.mdfStaffName,
      patanerName: staffType.patanerName,
      mdfMakerStaffName: staffType.mdfMakerStaffName,

      subSeasonCode: orderListSearchFormConditions.subSeason,
      year: orderListSearchFormConditions.year,
      productDeliveryAtYearFrom: orderListSearchFormConditions.productDeliveryAtYearFrom,
      productDeliveryAtMonthlyFrom: orderListSearchFormConditions.productDeliveryAtMonthlyFrom,
      productDeliveryAtYearTo: orderListSearchFormConditions.productDeliveryAtYearTo,
      productDeliveryAtMonthlyTo: orderListSearchFormConditions.productDeliveryAtMonthlyTo,

      idOrderBy: OrderBy.DESC
    } as OrderSearchConditions;
  }

  /**
   * 検索条件のキーワードを設定する.
   * @param orderListSearchFormConditions 検索Form入力値
   * @returns 発注検索APIの検索条件
   */
  private setKeywordSearchCondition(orderListSearchFormConditions: OrderListSearchFormConditions): OrderSearchConditions {
    let partNo = null;             // 品番
    let productName = null;        // 品名
    let brandCode = null;          // ブランドコード
    let itemCode = null;           // アイテムコード
    let maker = null;              // メーカー
    let orderNumberText = null;    // 発注No

    // キーワードの確認
    switch (orderListSearchFormConditions.keywordType) {
      case OrderKeywordType.ITEM_NO:   // 品番
        // PRD_0107 mod JFE Start
        partNo = (orderListSearchFormConditions.keyword) && orderListSearchFormConditions.keyword.replace(/-/g, '') ;
        // PRD_0107 mod JFE End
        break;
      case OrderKeywordType.ITEM_NAME: // 品名
        productName = orderListSearchFormConditions.keyword;
        break;
      case OrderKeywordType.BRAND:     // ブランドコード
        brandCode = orderListSearchFormConditions.keyword;
        break;
      case OrderKeywordType.ITEM_CODE: // アイテムコード
        itemCode = orderListSearchFormConditions.keyword;
        break;
      case OrderKeywordType.MAKER: // メーカー
        maker = orderListSearchFormConditions.keyword;
        break;
      case OrderKeywordType.ORDER_NO: // 発注No
        orderNumberText = orderListSearchFormConditions.keyword;
        break;

      default: // 指定がない場合(「キーワード」が選択)、すべての検索条件にセット
        partNo = orderListSearchFormConditions.keyword;              // 品番
        productName = orderListSearchFormConditions.keyword;         // 品名
        brandCode = orderListSearchFormConditions.keyword;           // ブランドコード
        itemCode = orderListSearchFormConditions.keyword;            // アイテムコード
        maker = orderListSearchFormConditions.keyword;               // メーカー
        orderNumberText = orderListSearchFormConditions.keyword;     // 発注No
    }
    return {
      partNo: partNo,
      productName: productName,
      brandCode: brandCode,
      itemCode: itemCode,
      maker: maker,
      orderNumberText: orderNumberText
    } as OrderSearchConditions;
  }

  /**
   * 検索条件の担当者を設定する.
   * @param orderListSearchFormConditions 検索Form入力値
   * @returns 発注検索APIの検索条件
   */
  private setStaffTypeSearchCondition(orderListSearchFormConditions: OrderListSearchFormConditions): OrderSearchConditions {
    let plannerName = null;   // 企画担当
    let mdfStaffName = null;  // 製造担当
    let patanerName = null;   // パターンナー

    let mdfMakerStaffName = null; // 生産メーカー担当者名
    const staffName = orderListSearchFormConditions.staffName;

    // 担当の確認
    switch (orderListSearchFormConditions.staffType) {
      case StaffType.PLANNING:    // 企画担当
        plannerName = staffName;
        break;
      case StaffType.PRODUCTION:  // 製造担当
        mdfStaffName = staffName;
        break;
      case StaffType.PATANER:     // パターンナー
        patanerName = staffName;
        break;
      case StaffType.MAKER:       // メーカー担当
        mdfMakerStaffName = staffName;
        break;
      default: // 指定がない場合(「担当」が選択)、すべての検索条件にセット
        plannerName = staffName;        // 企画担当
        mdfStaffName = staffName;       // 製造担当
        patanerName = staffName;        // パターンナー
        mdfMakerStaffName = staffName;  // 生産メーカー担当者名
    }
    return {
      plannerName: plannerName,
      mdfStaffName: mdfStaffName,
      patanerName: patanerName,
      mdfMakerStaffName: mdfMakerStaffName
    } as OrderSearchConditions;
  }

  /**
   * 発注一覧に表示するリストを設定する.
   * @param orderList 発注情報リスト
   */
  private setOrderList(orderList: VOrder[]): void {
    this.allOrderList = orderList;                  // 全品番リスト

    this.confirmPendingOrderList = this.getConfirmPendingOrderList(orderList);        // 受注確定待ちリスト
    this.mdPendingOrderList = this.getMdPendingOrderList(orderList);                  // 発注承認待ちリスト
    this.misleadingPendingOrderList = this.getMisleadingPendingOrderList(orderList);  // 優良誤認待ちリスト
    this.productionOrderList = this.getProductionOrderList(orderList);                // 生産中リスト
    this.productCompleteOrderList = this.getProductCompleteOrderList(orderList);      // 完納リスト

  }

  /**
   * 受注確定待ちリストを取得
   * 完納は除く
   * @param allOrderList 全発注情報リスト
   */
  private getConfirmPendingOrderList(allOrderList: VOrder[]): VOrder[] {
    return  allOrderList.filter(orderData => !this.isCompleteOrder(orderData) && !this.isConfirmOrderOk(orderData));
  }

  /**
   * 発注承認待ちリストを取得
   * 完納と受注確定待ちは除く
   * @param allOrderList 全発注情報リスト
   */
  private getMdPendingOrderList(allOrderList: VOrder[]): VOrder[] {
    return allOrderList.filter(orderData =>
      !this.isCompleteOrder(orderData) && this.isConfirmOrderOk(orderData) && !this.isMdApprovalOk(orderData));
  }

  /**
   * 優良誤認承認待ちリストを取得
   * 品番未確定、完納は除く
   * @param allOrderList 全発注情報リスト
   */
  private getMisleadingPendingOrderList(allOrderList: VOrder[]): VOrder[] {
    return allOrderList.filter(orderData =>
      !this.isCompleteOrder(orderData) && !BusinessCheckUtils.isQualityApprovalOk(orderData)
      && BusinessCheckUtils.isConfirmPartOk(orderData));
  }

  /**
   * 生産中リストを取得
   * 完納は除く
   * 優良誤認未承認でも生産中リストに含む
   * @param allOrderList 全発注情報リスト
   */
  private getProductionOrderList(allOrderList: VOrder[]): VOrder[] {
    return this.productionOrderList = allOrderList.filter(orderData => this.isMdApprovalOk(orderData) && !this.isCompleteOrder(orderData));
  }

  /**
   * 完納リストを取得
   * @param allOrderList 全発注情報リスト
   */
  private getProductCompleteOrderList(allOrderList: VOrder[]): VOrder[] {
    return this.productCompleteOrderList = allOrderList.filter(orderData => this.isCompleteOrder(orderData));
  }

  /**
   * 受注確定済か判定する.
   * @param orderData 発注情報
   * @returns 受注確定済であればtrue
   */
  isConfirmOrderOk(orderData: VOrder): boolean {
    return BusinessCheckUtils.isConfirmOrderOk(orderData);
  }

  /**
   * 発注承認済(発注承認済)か判定する.
   * @param orderData 発注情報
   * @returns 発注承認済であればtrue
   */
  isMdApprovalOk(orderData: VOrder): boolean {
    const oas = orderData.orderApproveStatus;
    return oas != null && oas === OrderApprovalStatus.ACCEPT;
  }

  /**
   * 納品依頼数を取得する.
   * @param orderData 発注情報
   * @returns 納品依頼数
   */
  getDeliverysCnt(orderData: VOrder): number {
    return !orderData.deliverys ? 0 : orderData.deliverys.length;
  }

  /**
   * 完納判定を行う.
   * @param orderData 発注情報
   * @returns 全済または完納または自動完納であればtrue
   */
  isCompleteOrder(orderData: VOrder): boolean {
    return BusinessCheckUtils.isCompleteOrder(orderData);
  }

  /**
   * 発注一覧リストのタブ変更時の処理.
   * @param event 選択タブの情報
   */
  onOrderListTabChange(event: NgbTabChangeEvent): void {
    this.submitted = false;
    // 選択タブをsessionに保持
    this.localStorageService.createLocalStorage(this.session, this.STORAGE_KEY_ORDER_LIST_SELECTED_TAB_ID, event.nextId);
  }

  /**
   * スクロール時、最下部までスクロールされた場合は、次の検索結果を取得します。
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    if (event.isWindowEvent || !event.isReachingBottom || StringUtils.isEmpty(this.nextPageToken)) {
      return;
    }

    // 一番下までスクロールされ、次のページのトークンがある場合、次のリストを取得する
    const nextPageToken = this.nextPageToken;
    this.nextPageToken = null;
    this.loadingService.loadStart();

    this.orderService.getOrderList({ pageToken: nextPageToken } as OrderSearchConditions).toPromise().then(
      genericList => {
        this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
        const itemList = genericList.items;
        if (itemList.length > 0) { // 検索結果あり
          // 検索結果1以上の場合はリストに設定する。
          this.addOrderList(itemList);
          this.loadingService.loadEnd();
        }
      },
      error => {
        this.handleApiError(error);
      }
    );
  }

  /**
   * 発注一覧に追加取得分をリストに設定する。
   * @param orderList 発注情報リスト
   */
  private addOrderList(orderList: VOrder[]): void {

    this.allOrderList = this.allOrderList.concat(orderList);   // 全発注リスト
    this.confirmPendingOrderList = this.confirmPendingOrderList.concat(this.getConfirmPendingOrderList(orderList));           // 受注確定待ちリスト
    this.mdPendingOrderList = this.mdPendingOrderList.concat(this.getMdPendingOrderList(orderList));                          // 発注承認待ちリスト
    this.misleadingPendingOrderList = this.misleadingPendingOrderList.concat(this.getMisleadingPendingOrderList(orderList));  // 優良誤認待ちリスト
    this.productionOrderList = this.productionOrderList.concat(this.getProductionOrderList(orderList));                       // 生産中リスト
    this.productCompleteOrderList = this.productCompleteOrderList.concat(this.getProductCompleteOrderList(orderList));        // 完納リスト
  }

  /**
   * エラーハンドリング
   * @param error
   */
  private handleApiError(error: any): void {
    this.overall_msg_code = '';
    const apiError = ExceptionUtils.apiErrorHandler(error);
    if (apiError != null) {
      if (apiError.viewErrors == null || apiError.viewErrors[0].viewErrorMessageCode == null) {
        ExceptionUtils.displayErrorInfo('defaultErrorInfo', apiError.viewErrorMessageCode);
      } else {
        apiError.viewErrors.some(errorDetailView => {
          switch (errorDetailView.code) {
            case '400_03':
              this.overall_msg_code = 'INFO.CONDITIONS_LIMIT';
              break;
            default:
              // それ以外はサーバエラーエリアにメッセージ表示
              ExceptionUtils.displayErrorInfo('defaultErrorInfo', errorDetailView.viewErrorMessageCode);
              return true; // loop終了
          }
        });
      }
    }
    this.loadingService.loadEnd();
  }
}
