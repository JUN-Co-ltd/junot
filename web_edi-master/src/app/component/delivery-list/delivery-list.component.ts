import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ScrollEvent } from 'ngx-scroll-event';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
// PRD_0011 add SIT start
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
// PRD_0011 add SIT end

import { StringUtils } from '../../util/string-utils';
import { ListUtils } from '../../util/list-utils';
import { NumberUtils } from '../../util/number-utils';
import { ExceptionUtils } from '../../util/exception-utils';
import { DateUtils } from '../../util/date-utils';
// PRD_0008 add SIT start
import { DateUtilsService } from 'src/app/service/bo/date-utils.service';
// PRD_0008 add SIT end

import { LoadingService } from '../../service/loading.service';
import { HeaderService } from '../../service/header.service';
import { SessionService } from '../../service/session.service';
import { LocalStorageService } from '../../service/local-storage.service';
import { DeliverySearchListService } from '../../service/delivery-search-list.service';
import { JunpcSirmstService } from '../../service/junpc-sirmst.service';
// PRD_0011 add SIT start
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
// PRD_0011 add SIT end

import { SearchSupplierModalComponent } from '../search-supplier-modal/search-supplier-modal.component';

import {
  DeliveryKeywordType, DeliveryListCarryType, DeliveryListOrderCompleteType,
  DeliveryListPurchasesType, DeliveryListApprovaldType, DeliveryListShipmentType,
  KeywordConditionsLimit, OrderBy, AuthType, DeliveryListAllocationStatus, CarryType, DeliveryApprovalStatus,
  DeliveryListReallocationType, Path, SupplierType, SearchTextType, ValidatorsPattern,
  // PRD_0037 add SIT start
  DeliveryListAllocationStatusType
  // PRD_0037 add SIT end
} from '../../const/const';

import { DeliveryListSearchFormConditions, DeliveryListSearchConditions } from '../../model/search-conditions';
import { DeliverySearchResult } from '../../model/delivery-search-result';
import { Session } from '../../model/session';
import { GenericList } from '../../model/generic-list';
import { JunpcSirmst } from '../../model/junpc-sirmst';
import { JunpcSirmstSearchCondition } from '../../model/junpc-sirmst-search-condition';
// PRD_0011 add SIT start
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { FormUtils } from 'src/app/util/form-utils';
// PRD_0011 add SIT end

@Component({
  selector: 'app-delivery-list',
  templateUrl: './delivery-list.component.html',
  styleUrls: ['./delivery-list.component.scss']
})
export class DeliveryListComponent implements OnInit {
  private readonly STORAGE_KEY_DELIVERY_SEARCH_CONDITIONS = 'deliveryListSearchFormConditions'; // 検索条件のStorageKey

  // htmlからの定数値参照
  readonly PATH = Path;
  readonly AUTH_TYPE = AuthType;
  readonly DELIVERY_KEYWORD_TYPE = DeliveryKeywordType;
  readonly DELIVERY_CARRY_TYPE = DeliveryListCarryType;
  readonly DELIVERY_ORDER_COMPLETE_TYPE = DeliveryListOrderCompleteType;
  readonly DELIVERY_PURCHASES_TYPE = DeliveryListPurchasesType;
  readonly DELIVERY_APPROVALD_TYPE = DeliveryListApprovaldType;
  readonly DELIVERY_APPROVAL_STATUS = DeliveryApprovalStatus;
  readonly DELIVERY_SHIPMENT_TYPE = DeliveryListShipmentType;
  readonly DELIVERY_REALLOCATION_TYPE = DeliveryListReallocationType;
  readonly SUPPLIER_TYPE = SupplierType;
  readonly VALIDATORS_PATTERN = ValidatorsPattern;
  // PRD_0037 add SIT start
  readonly DELIVERY_ALLOCATION_STATUS_TYPE = DeliveryListAllocationStatusType;
  // PRD_0037 add SIT end

  private readonly CSS_BLUE = 'font-blue';
  private readonly CSS_RED = 'font-red';
  private readonly CSS_GREEN = 'font-green';

  private session: Session; // セッション情報
  affiliation: AuthType; // ログインユーザの情報
  company = ''; // ログインユーザーの会社名

  formConditions: DeliveryListSearchFormConditions = new DeliveryListSearchFormConditions();  // 配分一覧検索Formの入力値
  private nextPageToken = '';  // 次のページのトークン
  allocationStatusCls: string; // 配分状態CSS

  /** PRD_0011 add SIT start */
  departments: JunpcCodmst[] = []; // 事業部リスト
  /** PRD_0011 add SIT end */

  deliverySearchResultList: DeliverySearchResult[] = []; // 配分一覧(納品依頼)検索結果リスト

  showValidationError = false;  // バリデーションエラー表示フラグ ※htmlで不使用
  overallMsgCode = '';  // エラーメッセージ

  isSearchBtnLock = true;  // 検索処理中にボタンをロックするためのフラグ

  constructor(
    private sessionService: SessionService,
    private headerService: HeaderService,
    private localStorageService: LocalStorageService,
    private loadingService: LoadingService,
    private deliverySearchListService: DeliverySearchListService,
    private junpcSirmstService: JunpcSirmstService,
    // PRD_0011 add SIT start
    private junpcCodmstService: JunpcCodmstService,
    // PRD_0011 add SIT end
    // PRD_0008 add SIT start
    private dateUtilsService: DateUtilsService,
    // PRD_0008 add SIT end
    private modalService: NgbModal
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.session = this.sessionService.getSaveSession();  // ログインユーザの情報を取得する
    this.affiliation = this.session.affiliation;
    this.company = this.session.company;

    // PRD_0011 add SIT start
    // 事業部リストを取得する
    forkJoin(
      this.getDivisionList(),
    ).pipe().subscribe();
    // PRD_0011 add SIT end

    this.setInitDataToForm(this.session).then(() => {
      this.setSuppliersInfoByAuth();
      this.onSearch();
    });
  }

  /**
   * Formに初期値を設定する.
   * storageから検索条件が取得されなかった場合、何も設定しない
   * @param session セッション
   * @returns Promise<boolean> 設定成否
   */
  private async setInitDataToForm(session: Session): Promise<boolean> {
    // 前回のform入力値取得
    const storageConditions = JSON.parse( // storageから前回入力した検索条件を取得する。
      this.localStorageService.getSaveLocalStorage(session, this.STORAGE_KEY_DELIVERY_SEARCH_CONDITIONS));

    if (storageConditions != null) {
      this.formConditions = storageConditions;
      // PRD_0008 del SIT start
      //return Promise.resolve(true);
      // PRD_0008 del SIT end
    }

    // PRD_0008 add SIT start
    // PRD_0040 add SIT start
    if (FormUtils.isEmpty(this.formConditions.deliveryAtFrom) && FormUtils.isEmpty(this.formConditions.deliveryAtTo)) {
      // PRD_0040 add SIT end
      // 納品日fromの初期値は当日日付
      this.formConditions.deliveryAtFrom = this.dateUtilsService.generateCurrentDate();
      // 納品日toの初期値は当日日付+14日
      this.formConditions.deliveryAtTo = this.dateUtilsService.generateCurrentAddDayDate(14);
    // PRD_0040 add SIT start
    }
    // PRD_0040 add SIT end
    return Promise.resolve(true);
    // PRD_0008 add SIT end
  }

  /**
   * 配分一覧検索処理.
   * @param searchForm form情報
   */
  onSearch(searchForm?: NgForm): void {
    this.isSearchBtnLock = true;
    if (this.isValidatorError(searchForm)) {
      this.isSearchBtnLock = false;
      return;
    }
    this.loadingService.loadStart();
    this.deliverySearchResultList = [];  // 配分一覧検索結果リストをクリア
    this.overallMsgCode = '';   // エラーメッセージをクリア
    this.nextPageToken = null;  // トークンクリア

    // キーワードの個数チェック
    if (StringUtils.isNotEmpty(this.formConditions.keyword)) {
      const result = this.formConditions.keyword.split(/[\x20\u3000]/);
      if (result.length > KeywordConditionsLimit.DELIVERY_SEARCH_LIST) {
        this.overallMsgCode = 'INFO.CONDITIONS_LIMIT';
        this.loadingService.loadEnd();
        return;
      }
    }

    // 検索条件をstorageに保持
    this.saveFormConditions();

    // 入力値をAPI検索用Modelに設定する。
    const deliveryListSearchApiConditions = this.generateApiSearchConditions(this.formConditions);

    this.generateDeliverySearchResultList(deliveryListSearchApiConditions);
  }

  /**
   * 検索条件をstorageに保持.
   */
  private saveFormConditions(): void {
    this.localStorageService.createLocalStorage(this.session, this.STORAGE_KEY_DELIVERY_SEARCH_CONDITIONS, this.formConditions);
  }

  /**
   * 検索Form入力値をAPI検索用Modelに設定して返す.
   * @param deliveryListSearchFormConditions 検索Form入力値
   * @returns API検索用Model
   */
  private generateApiSearchConditions(deliveryListSearchFormConditions: DeliveryListSearchFormConditions): DeliveryListSearchConditions {
    // キーワード設定
    const keyword = this.setKeywordSearchCondition(deliveryListSearchFormConditions);
    // 内訳(キャリー区分)設定
    const carryType = this.setCarryTypeSearchCondition(deliveryListSearchFormConditions);
    // 完納設定
    const orderCompleteFlg = this.setOrderCompleteTypeSearchCondition(deliveryListSearchFormConditions);
    // 仕入設定
    const purchasesFlg = this.setPurchasesTypeSearchCondition(deliveryListSearchFormConditions);
    // 承認設定
    const approvaldFlg = this.setApprovaldTypeSearchCondition(deliveryListSearchFormConditions);
    // 出荷設定
    const shipmentFlg = this.setShipmentTypeSearchCondition(deliveryListSearchFormConditions);
    // PRD_0037 del SIT start
    //// 要再配分
    //const reAllocationFlg = deliveryListSearchFormConditions.reAllocation;
    // PRD_0037 del SIT end
    // 空文字が入る可能性のある項目。空文字の場合はnullをセット
    const mdfMakerCode = deliveryListSearchFormConditions.mdfMakerCode;
    const orderNumberFrom = StringUtils.toStringSafe(deliveryListSearchFormConditions.orderNumberFrom);
    const orderNumberTo = StringUtils.toStringSafe(deliveryListSearchFormConditions.orderNumberTo);
    const deliveryNumberFrom = deliveryListSearchFormConditions.deliveryNumberFrom;
    const deliveryNumberTo = deliveryListSearchFormConditions.deliveryNumberTo;
    const deliveryRequestNumberFrom = deliveryListSearchFormConditions.deliveryRequestNumberFrom;
    const deliveryRequestNumberTo = deliveryListSearchFormConditions.deliveryRequestNumberTo;
    // PRD_0011 add SIT start
    const departmentCode = deliveryListSearchFormConditions.departmentCode;
    // PRD_0011 add SIT end

    return {
      partNo: keyword.partNo,
      brandCode: keyword.brandCode,
      // PRD_0011 mod SIT start
      //departmentCode: keyword.departmentCode,
      departmentCode: StringUtils.isNotEmpty(departmentCode) ? departmentCode : null,
      // PRD_0011 mod SIT end
      carryType: carryType,
      orderCompleteFlg: orderCompleteFlg,
      purchasesFlg: purchasesFlg,
      approvaldFlg: approvaldFlg,
      shipmentFlg: shipmentFlg,
      // PRD_0037 mod SIT start
      //reAllocationFlg: reAllocationFlg,
      allocationStatusType: deliveryListSearchFormConditions.allocationStatusType === undefined ? null : deliveryListSearchFormConditions.allocationStatusType,
      // PRD_0037 mod SIT end
      mdfMakerCode: StringUtils.isNotEmpty(mdfMakerCode) ? mdfMakerCode : null,
      deliveryAtFrom: deliveryListSearchFormConditions.deliveryAtFrom,
      deliveryAtTo: deliveryListSearchFormConditions.deliveryAtTo,
      orderNumberFrom:
        StringUtils.isNotEmpty(orderNumberFrom) ? NumberUtils.toInteger(deliveryListSearchFormConditions.orderNumberFrom) : null,
      orderNumberTo:
        StringUtils.isNotEmpty(orderNumberTo) ? NumberUtils.toInteger(deliveryListSearchFormConditions.orderNumberTo) : null,
      deliveryNumberFrom: StringUtils.isNotEmpty(deliveryNumberFrom) ? deliveryNumberFrom : null,
      deliveryNumberTo: StringUtils.isNotEmpty(deliveryNumberTo) ? deliveryNumberTo : null,
      deliveryRequestNumberFrom: StringUtils.isNotEmpty(deliveryRequestNumberFrom) ? deliveryRequestNumberFrom : null,
      deliveryRequestNumberTo: StringUtils.isNotEmpty(deliveryRequestNumberTo) ? deliveryRequestNumberTo : null,

    } as DeliveryListSearchConditions;
  }

  /**
   * 検索条件のキーワードを設定する.
   * @param deliveryListSearchFormConditions 検索Form入力値
   * @returns 配分一覧検索APIの検索条件
   */
  private setKeywordSearchCondition(deliveryListSearchFormConditions: DeliveryListSearchFormConditions): DeliveryListSearchConditions {
    let partNo = null;  // 品番
    let brandCode = null; // ブランドコード
    // PRD_0011 del SIT start
    //let departmentCode = null; // 事業部
    // PRD_0011 del SIT end

    // キーワードの確認
    switch (deliveryListSearchFormConditions.keywordType) {
      case DeliveryKeywordType.ITEM_NO: // 品番
        partNo = deliveryListSearchFormConditions.keyword;
        break;
      case DeliveryKeywordType.BRAND: // ブランドコード
        brandCode = deliveryListSearchFormConditions.keyword;
        break;
      // PRD_0011 del SIT start
      //case DeliveryKeywordType.DEPARTMENT_CODE: // 事業部
      //  departmentCode = deliveryListSearchFormConditions.keyword;
      //   break;
      // PRD_0011 del SIT end
      default: // 指定がない場合(「キーワード」が選択)、すべての検索条件にセット
        partNo = deliveryListSearchFormConditions.keyword; // 品番
        brandCode = deliveryListSearchFormConditions.keyword; // ブランドコード
        // PRD_0011 del SIT start
        //departmentCode = deliveryListSearchFormConditions.keyword; // 事業部
        // PRD_0011 del SIT end
    }
    // PRD_0011 mod SIT start
    //return { partNo: partNo, brandCode: brandCode, departmentCode: departmentCode } as DeliveryListSearchConditions;
    return { partNo: partNo, brandCode: brandCode } as DeliveryListSearchConditions;
    // PRD_0011 mod SIT end
  }

  /**
   * 検索条件の内訳(キャリー区分)を設定する.
   * @param deliveryListSearchFormConditions 検索Form入力値
   * @returns 内訳(キャリー区分)
   */
  private setCarryTypeSearchCondition(deliveryListSearchFormConditions: DeliveryListSearchFormConditions): String {

    // 内訳(キャリー区分)の確認
    switch (deliveryListSearchFormConditions.carryType) {
      case DeliveryListCarryType.NORMAL: // 通常
        return CarryType.NORMAL;
      case DeliveryListCarryType.DIRECT: // 直送
        return CarryType.DIRECT;
      // PRD_0104#7055 add JFE start
        case DeliveryListCarryType.TC: // 直送
        return CarryType.TC;
      // PRD_0104#7055 add JFE end
      default: // 指定がない場合(「空白」が選択)、null
        break;
    }
    return null;
  }

  /**
   * 検索条件の完納区分を設定する.
   * @param deliveryListSearchFormConditions 検索Form入力値
   * @returns 完納フラグ false：未完(0)、true:完納(1)
   */
  private setOrderCompleteTypeSearchCondition(deliveryListSearchFormConditions: DeliveryListSearchFormConditions): boolean {

    // 完納フラグの確認
    switch (deliveryListSearchFormConditions.orderCompleteType) {
      case DeliveryListOrderCompleteType.INCOMPLETE: // 未完
        return false;
      case DeliveryListOrderCompleteType.COMPLETE: // 完納
        return true;
      default: // 指定がない場合(「空白」が選択)、null
        break;
    }
    return null;
  }

  /**
   * 検索条件の仕入区分を設定する.
   * @param deliveryListSearchFormConditions 検索Form入力値
   * @returns 仕入フラグ false：未仕入(0)、true:仕入済(1)
   */
  private setPurchasesTypeSearchCondition(deliveryListSearchFormConditions: DeliveryListSearchFormConditions): boolean {

    // 仕入フラグの確認
    switch (deliveryListSearchFormConditions.purchasesType) {
      case DeliveryListPurchasesType.UNPURCHASE: // 未仕入
        return false;
      case DeliveryListPurchasesType.PURCHASE: // 仕入済
        return true;
      default: // 指定がない場合(「空白」が選択)、null
        break;
    }
    return null;
  }

  /**
   * 検索条件の承認区分を設定する.
   * @param deliveryListSearchFormConditions 検索Form入力値
   * @returns 承認フラグ false：未承認(0)、true:承認済(1)
   */
  private setApprovaldTypeSearchCondition(deliveryListSearchFormConditions: DeliveryListSearchFormConditions): boolean {

    // 承認フラグの確認
    switch (deliveryListSearchFormConditions.approvaldType) {
      case DeliveryListApprovaldType.UNAPPROVALD: // 未承認
        return false;
      case DeliveryListApprovaldType.APPROVALD: // 承認済
        return true;
      default: // 指定がない場合(「空白」が選択)、null
        break;
    }
    return null;
  }

  /**
   * 検索条件の出荷区分を設定する.
   * @param deliveryListSearchFormConditions 検索Form入力値
   * @returns 出荷フラグ false：未出荷(0)、true:出荷済(1)
   */
  private setShipmentTypeSearchCondition(deliveryListSearchFormConditions: DeliveryListSearchFormConditions): boolean {

    // 出荷フラグの確認
    switch (deliveryListSearchFormConditions.shipmentType) {
      case DeliveryListShipmentType.UNSHIPMENT: // 未出荷
        return false;
      case DeliveryListShipmentType.SHIPMENT: // 出荷済
        return true;
      default: // 指定がない場合(「空白」が選択)、null
        break;
    }
    return null;
  }

  // PRD_0011 add SIT start
  /**
   * 事業部リストを取得する.
   * @returns Observable<JunpcCodmst[]>
   */
   private getDivisionList(): Observable<JunpcCodmst[]> {
    return this.junpcCodmstService.getAllDivisions().pipe(
      map(list => this.departments = list.items)
    );
   }
  // PRD_0011 add SIT start

  /**
   * スクロール時、最下部までスクロールされた場合は、次の検索結果を取得.
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    if (event.isWindowEvent || !event.isReachingBottom || StringUtils.isEmpty(this.nextPageToken)) {
      return;
    }
    // 一番下までスクロールされ、次のページのトークンがある場合、次のリストを取得する
    this.loadingService.loadStart();
    const nextPageToken = this.nextPageToken;
    this.nextPageToken = null;

    this.generateDeliverySearchResultList({ pageToken: nextPageToken } as DeliveryListSearchConditions);
  }

  /**
   * 配分一覧検索結果取得処理.
   * @param deliveryListSearchApiConditions 配分一覧API検索用Model
   */
  private generateDeliverySearchResultList(deliveryListSearchApiConditions: DeliveryListSearchConditions): void {
    this.deliverySearchListService.listDeliverySearchResultList(deliveryListSearchApiConditions).subscribe(
      genericList => this.setDeliverySearchResultData(genericList),
      error => this.handleApiError(error));
  }

  /**
   * 配分一覧検索結果取得後の設定処理.
   * @param genericList 配分一覧検索結果リスト
   */
  private setDeliverySearchResultData(genericList: GenericList<DeliverySearchResult>): void {
    const deliverySearchResultList = genericList.items;
    console.debug('nextPage result:', deliverySearchResultList);
    this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
    deliverySearchResultList.forEach(deliverySearchResult => {
      // 初期値設定
      deliverySearchResult.allocationStatus = this.generateAllocationStatus(deliverySearchResult);
    });
    this.deliverySearchResultList = this.deliverySearchResultList.concat(deliverySearchResultList);
    this.loadingService.loadEnd();
    this.isSearchBtnLock = false;
  }

  /**
   * 配分状態の設定.
   * 指示済未出荷：納品依頼に紐付く出荷指示送信が完了し、出荷確定が未取込の場合
   * 出荷済　　　：納品依頼に紐付く出荷確定の取込が完了している場合
   * 要再配分　　：入荷フラグ = true:入荷済 かつ 店舗配分済 かつ 仕入確定数 < 配分数の場合
   * 仕入済　　　：入荷フラグ = true:入荷済 かつ 店舗配分済 かつ 仕入確定数 >= 配分数の場合
   * 仕入済未配分：入荷フラグ = true:入荷済 かつ 店舗未配分の場合
   * 配分済　　　：納品情報.承認ステータス = 1:承認 かつ 店舗配分済の場合
   * 承認済　　　：納品情報.承認ステータス = 1:承認 かつ 店舗未配分の場合
   * 未承認　　　：納品情報.承認ステータス = 0:未承認  または 2:差し戻しの場合
   *
   * @param deliverySearchResult 検索結果
   */
  private generateAllocationStatus(deliverySearchResult: DeliverySearchResult): string {
    const deliveryApproveStatus = deliverySearchResult.deliveryApproveStatus;
    const fixArrivalCount = deliverySearchResult.fixArrivalCount;
    const allocationLot = deliverySearchResult.allocationLot;
    const arrivalFlg = deliverySearchResult.arrivalFlg;
    const shippingInstructionsFlg = deliverySearchResult.shippingInstructionsFlg;
    const storeRegisteredFlg = deliverySearchResult.storeRegisteredFlg;
    // PRD_0087 add SIT start
    const allocationCompleteAt = deliverySearchResult.allocationCompleteAt;
    const allocationRecordAt = deliverySearchResult.allocationRecordAt;
    // PRD_0087 add SIT end

    if (shippingInstructionsFlg) {
      // PRD_0087 mod SIT start
      //// 出荷済
      //return DeliveryListAllocationStatus.SHIPMENT;
      if (FormUtils.isEmpty(allocationCompleteAt) && FormUtils.isEmpty(allocationRecordAt)) {
        // 指示済未出荷
        return DeliveryListAllocationStatus.SHIPMENT_INSTRUCTION;
      } else {
        // 出荷済
        return DeliveryListAllocationStatus.SHIPMENT;
      }
      // PRD_0087 mod SIT end
    }
    if (arrivalFlg) {
      // PRD_0032 mod SIT start
      //// 仕入済
      //return DeliveryListAllocationStatus.PURCHASE;
      if (storeRegisteredFlg) {
        if (fixArrivalCount < allocationLot) {
          // 要再配分
          return DeliveryListAllocationStatus.REALLOCATION;
        } else {
          // 仕入済
          return DeliveryListAllocationStatus.PURCHASE;
        }
      } else {
        // 仕入済未配分
        return DeliveryListAllocationStatus.UNALLOCATED;
      }
      // PRD_0032 mod SIT end
    }
    if (deliveryApproveStatus === DeliveryApprovalStatus.ACCEPT) {
      if (storeRegisteredFlg) {
        // 配分済
        return DeliveryListAllocationStatus.ALLOCATED;
      } else {
        // 承認済
        return DeliveryListAllocationStatus.APPROVED;
      }
    }
    // PRD_0032 del SIT start
    //if (fixArrivalCount < allocationLot) {
    //  // 要再配分
    //  return DeliveryListAllocationStatus.REALLOCATION;
    //}
    // PRD_0032 del SIT end
    // 未承認：
    return DeliveryListAllocationStatus.UNAPPROVED;
  }

  /**
   * エラーハンドリング.
   * @param error
   */
  private handleApiError(error: any): void {
    this.overallMsgCode = '';
    const apiError = ExceptionUtils.apiErrorHandler(error);
    if (apiError != null) {
      if (apiError.viewErrors == null || apiError.viewErrors[0].viewErrorMessageCode == null) {
        ExceptionUtils.displayErrorInfo('defaultErrorInfo', apiError.viewErrorMessageCode);
      } else {
        apiError.viewErrors.find(errorDetailView => {
          switch (errorDetailView.code) {
            case '400_03':
              this.overallMsgCode = 'INFO.CONDITIONS_LIMIT';
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
    this.isSearchBtnLock = false;
  }

  /**
   * バリデーションエラーのチェック.
   * @param searchForm 検索条件入力値
   * @returns true:バリデーションエラー,false:エラーなし
   */
  private isValidatorError(searchForm: NgForm): boolean {
    if (searchForm == null) {
      return false;
    }

    if (searchForm.invalid) {
      this.showValidationError = true; // htmlで不使用
      return true; // validationエラーがある場合終了
    }

    this.showValidationError = false; // htmlで不使用
    return false;
  }

  /**
   * 権限により取引先情報を設定する.
   * 取引先権限の場合、自分の所属会社で生産メーカーを固定にする為、
   * 取引先情報を取得する。
   */
  private setSuppliersInfoByAuth(): void {
    if (this.affiliation === AuthType.AUTH_SUPPLIERS) {
      this.junpcSirmstService.getSirmst({
        sirkbn: SupplierType.MDF_MAKER,
        searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH, // Ph2で完全一致検索対応
        searchText: this.company
      } as JunpcSirmstSearchCondition).subscribe(sirMst => {
        // マスタの配列に設定
        this.formConditions.mdfMakerCode = sirMst.items[0]['sire'];
        this.formConditions.mdfMakerName = sirMst.items[0]['name'];
      });
    }
  }

  /**
   * 仕入先変更時の処理.
   */
  onChangeMaker(): void {
    // 入力項目毎のコード取得
    const mdfMakerCode = this.formConditions.mdfMakerCode;

    // 最大長まで入力されていない場合は検索しない
    if (mdfMakerCode.length !== 5) {
      this.formConditions.mdfMakerName = null;
      return;
    }

    // 仕入れマスタ取得APIコール
    this.junpcSirmstService.getSirmst({
      sirkbn: SupplierType.MDF_MAKER,
      searchType: SearchTextType.CODE_PARTIAL_MATCH,
      searchText: mdfMakerCode
    } as JunpcSirmstSearchCondition).subscribe(sirMst => {
      let setValue = '';  // 結果が取得できない場合は初期化
      if (sirMst != null && ListUtils.isNotEmpty(sirMst.items)) {
        setValue = sirMst.items[0].name; // 結果が取得出来たらそれぞれのName項目にセット
      }
      this.formConditions.mdfMakerName = setValue;
    });
  }

  /**
   * 仕入先検索モーダルを表示する.
   */
  openSearchSupplierModal(): void {
    const modalRef = this.modalService.open(SearchSupplierModalComponent);

    // モーダルへ渡す値を設定する
    const paramSearchText = this.formConditions.mdfMakerCode;
    const paramSire = this.formConditions.mdfMakerCode;
    const paramName = this.formConditions.mdfMakerName;
    console.debug('mdfMakerCode', paramSearchText);

    modalRef.componentInstance.searchCondition = {
      sirkbn: SupplierType.MDF_MAKER,
      searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH,
      searchText: paramSearchText
    } as JunpcSirmstSearchCondition;
    modalRef.componentInstance.default = { sire: paramSire, name: paramName } as JunpcSirmst;

    // モーダルからの値を設定する
    modalRef.result.then((result: JunpcSirmst) => {
      if (result) {
        this.formConditions.mdfMakerCode = result.sire;
        this.formConditions.mdfMakerName = result.name;
      }
    }, () => { });
  }

  /**
   * 日付フォーカスアウト時処理.
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string): void {
    // pipeの変換値がformにセットされないので別途格納
    const ngbDate = DateUtils.onBlurDate(maskedValue);
    if (ngbDate) { this.formConditions[type] = ngbDate; }
  }

  /**
   * 配分状態のCSSを設定する.
   * @param allocationStatus 配分状態
   */
  setAllocationStatusCls(allocationStatus: string): string {
    switch (allocationStatus) {
      case DeliveryListAllocationStatus.ALLOCATED: // 配分済：青文字
        return this.CSS_BLUE;
      case DeliveryListAllocationStatus.REALLOCATION: // 要再配分：赤文字
        return this.CSS_RED;
      case DeliveryListAllocationStatus.APPROVED: // 承認済：緑文字
        return this.CSS_GREEN;
      case DeliveryListAllocationStatus.UNAPPROVED: // 未承認：黒文字(デフォルト)
      case DeliveryListAllocationStatus.PURCHASE: // 仕入済：黒文字(デフォルト)
      case DeliveryListAllocationStatus.SHIPMENT: // 出荷済：黒文字(デフォルト)
      // PRD_0032 add SIT start
      case DeliveryListAllocationStatus.UNALLOCATED: // 仕入済未配分：黒文字(デフォルト)
      // PRD_0032 add SIT end
      // PRD_0087 add SIT start
      case DeliveryListAllocationStatus.SHIPMENT_INSTRUCTION: // 指示済未出荷：黒文字(デフォルト)
      // PRD_0087 add SIT end
      default:
        break;
    }
  }

}
