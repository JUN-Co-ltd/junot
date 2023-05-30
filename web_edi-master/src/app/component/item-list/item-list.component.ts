import { Component, OnInit, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { NgbTabChangeEvent, NgbTabset } from '@ng-bootstrap/ng-bootstrap';

import { ScrollEvent } from 'ngx-scroll-event';

import {
  AuthType, OrderApprovalStatus, ViewMode, StaffType, KeywordType, RegistStatus, ItemListPageTabName, KeywordConditionsLimit, OrderBy
} from '../../const/const';
import { CodeMaster } from '../../const/code-master';
import { ExceptionUtils } from '../../util/exception-utils';
import { StringUtils } from '../../util/string-utils';

import { SessionService } from '../../service/session.service';
import { LocalStorageService } from '../../service/local-storage.service';
import { ItemService } from '../../service/item.service';
import { LoadingService } from '../../service/loading.service';

import { Item } from '../../model/item';
import { ItemSearchConditions, ItemListSearchFormConditions } from '../../model/search-conditions';
import { Session } from '../../model/session';

@Component({
  selector: 'app-item-list',
  templateUrl: './item-list.component.html',
  styleUrls: ['./item-list.component.scss']
})
export class ItemListComponent implements OnInit {
  @ViewChild('tabSet') ngbTabset: NgbTabset;

  // htmlからの定数値参照
  readonly AUTH_INTERNAL = AuthType.AUTH_INTERNAL;
  readonly STAFF_TYPE = StaffType;
  readonly KEYWORD_TYPE = KeywordType;
  readonly TAB_NAME = ItemListPageTabName;
  readonly ORDER_APPROVAL_STATUS = OrderApprovalStatus;
  readonly VIEW_MODE = ViewMode;

  private session: Session; // セッション情報
  affiliation: AuthType;  // ログインユーザの権限
  readonly STORAGE_KEY_ITEM_LIST_SEARCH_CONDITIONS = 'itemListSearchFormConditions'; // 検索条件のSessionKey
  readonly STORAGE_KEY_ITEM_LIST_SELECTED_TAB_ID = 'itemListSelectedTabId';  // 選択タブのSessionKey
  itemListSearchFormConditions: ItemListSearchFormConditions = null;  // 品番一覧検索Formの入力値
  nextPageToken: string;                                              // 次のページのトークン

  overall_msg_code = '';  // エラーメッセージ
  seasonMasterList: { id: number, value: string }[] = [];  // シーズンリスト

  allItemList: Item[] = [];                   // 全品番リスト
  makerRegisteredList: Item[] = [];           // メーカー登録済リスト
  registeredPartNoList: Item[] = [];          // 品番登録済リスト

  constructor(
    private sessionService: SessionService,
    private localStorageService: LocalStorageService,
    private itemService: ItemService,
    private loadingService: LoadingService
  ) { }

  ngOnInit() {
    this.session = this.sessionService.getSaveSession();  // ログインユーザの情報を取得する
    this.itemListSearchFormConditions = JSON.parse( // sessionから前回入力した検索条件を取得する。
      this.localStorageService.getSaveLocalStorage(this.session, this.STORAGE_KEY_ITEM_LIST_SEARCH_CONDITIONS));
    this.affiliation = this.session.affiliation;

    this.seasonMasterList = CodeMaster.subSeason; // シーズンマスタ設定

    // sessionから検索条件が取得されなかった場合、初期設定する
    if (this.itemListSearchFormConditions == null) {
      this.itemListSearchFormConditions = new ItemListSearchFormConditions(0, null, 0, null, null, null);
      this.itemListSearchFormConditions.staffName = String(this.session.accountName);
    }
    // 品番検索処理
    this.onItemSearch(this.itemListSearchFormConditions);
    this.ngbTabset.activeId = JSON.parse(
      this.localStorageService.getSaveLocalStorage(this.session, this.STORAGE_KEY_ITEM_LIST_SELECTED_TAB_ID));
  }

  /**
   * 検索ボタン押下時の処理.
   * @param orderListSearchFormConditions 品番情報検索API用Model
   * @param searchForm 検索Form
   */
  onClickSearchBtn(itemListSearchFormConditions: ItemListSearchFormConditions, searchForm: NgForm): void {
    if (searchForm && searchForm.invalid) { return; } // validationエラーがある場合終了
    this.loadingService.loadStart();
    this.onItemSearch(itemListSearchFormConditions);
  }

  /**
   * 品番検索処理を行う。
   * @param itemListSearchFormConditions 品番情報検索API用Model
   * @param searchForm 検索Form
   */
  onItemSearch(itemListSearchFormConditions: ItemListSearchFormConditions): void {
    this.allItemList = [];          // 全品番リストをクリア
    this.makerRegisteredList = [];  // メーカー登録済リストをクリア
    this.registeredPartNoList = []; // 品番登録済リストをクリア
    this.overall_msg_code = '';     // エラーメッセージをクリア

    // 入力値をAPI検索用Modelに設定する。
    const itemRequestApiConditions = this.generateApiSearchConditions(itemListSearchFormConditions);

    // キーワードの個数チェック
    if (StringUtils.isNotEmpty(itemListSearchFormConditions.keyword)) {
      const result = itemListSearchFormConditions.keyword.split(/[\x20\u3000]/);
      if (result.length > KeywordConditionsLimit.ITEM_LIST) {
        this.overall_msg_code = 'INFO.CONDITIONS_LIMIT';
        this.loadingService.loadEnd();
        return;
      }
    }

    // ソート順を指定
    itemRequestApiConditions.idOrderBy = OrderBy.DESC;

    // 品番情報検索処理
    this.itemService.getItemSearch(itemRequestApiConditions).toPromise().then(
      item => {
        this.nextPageToken = item.nextPageToken; // 次のページのトークンを保存する
        const itemList = item['items'];
        if (itemList.length <= 0) { // 検索結果なし
          this.overall_msg_code = 'INFO.RESULT_ZERO';
        } else {
        // 検索結果1以上の場合はリストに設定する。
        this.setItemList(itemList);
        }

        this.loadingService.loadEnd();
      },
      error => {
        this.handleApiError(error);
      }
    );
  }

  /**
   * 検索Form入力値をAPI検索用Modelに設定して返す。
   * @param itemListSearchFormConditions 検索Form入力値
   */
  private generateApiSearchConditions(itemListSearchFormConditions: ItemListSearchFormConditions): ItemSearchConditions {
    // キーワード、担当者設定
    const keyword = this.setKeywordSearchCondition(itemListSearchFormConditions);
    const staffType = this.setStaffTypeSearchCondition(itemListSearchFormConditions);
    // 検索条件をsessionに保持
    this.localStorageService.createLocalStorage(this.session, this.STORAGE_KEY_ITEM_LIST_SEARCH_CONDITIONS, itemListSearchFormConditions);
    return {
      partNo: keyword.partNo,
      productName: keyword.productName,
      brandCode: keyword.brandCode,
      itemCode: keyword.itemCode,
      plannerName: staffType.plannerName,
      mdfStaffName: staffType.mdfStaffName,
      patanerName: staffType.patanerName,
      mdfMakerStaffName: staffType.mdfMakerStaffName,
      subSeasonCode: itemListSearchFormConditions.subSeason,
      year: itemListSearchFormConditions.year
    } as ItemSearchConditions;
  }

  /**
   * 検索条件のキーワードを設定して返す。
   * @param itemListSearchFormConditions 検索Form入力値
   */
  private setKeywordSearchCondition(itemListSearchFormConditions: ItemListSearchFormConditions): ItemSearchConditions {
    let partNo = null;      // 品番
    let productName = null; // 品名
    let brandCode = null;   // ブランドコード
    let itemCode = null;    // アイテムコード

    // キーワードの確認
    switch (itemListSearchFormConditions.keywordType) {
      case KeywordType.ITEM_NO:   // 品番
        partNo = itemListSearchFormConditions.keyword.replace(/-/g, '');
        break;
      case KeywordType.ITEM_NAME: // 品名
        productName = itemListSearchFormConditions.keyword;
        break;
      case KeywordType.BRAND:     // ブランドコード
        brandCode = itemListSearchFormConditions.keyword;
        break;
      case KeywordType.ITEM_CODE: // アイテムコード
        itemCode = itemListSearchFormConditions.keyword;
        break;
      default: // 指定がない場合(「キーワード」が選択)、すべての検索条件にセット
        partNo = itemListSearchFormConditions.keyword;      // 品番
        productName = itemListSearchFormConditions.keyword; // 品名
        brandCode = itemListSearchFormConditions.keyword;   // ブランドコード
        itemCode = itemListSearchFormConditions.keyword;    // アイテムコード
    }
    return { partNo: partNo, productName: productName, brandCode: brandCode, itemCode: itemCode } as ItemSearchConditions;
  }

  /**
   * 検索条件の担当者を設定して返す。
   * @param itemListSearchFormConditions 検索Form入力値
   */
  private setStaffTypeSearchCondition(itemListSearchFormConditions: ItemListSearchFormConditions): ItemSearchConditions {
    let plannerName = null;     // 企画担当
    let mdfStaffName = null;    // 製造担当
    let patanerName = null;     // パターンナー
    let mdfMakerStaffName = null; // 生産メーカー担当者名
    const staffName = itemListSearchFormConditions.staffName;

    // 担当の確認
    switch (itemListSearchFormConditions.staffType) {
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
    } as ItemSearchConditions;
  }

  /**
   * 品番一覧に表示するリストを設定する。
   * @param itemList 品番情報リスト
   */
  private setItemList(itemList: Item[]): void {
    this.allItemList = itemList;  // 全品番リスト
    this.makerRegisteredList = itemList.filter(item => item.registStatus === RegistStatus.ITEM);  // メーカー登録済リスト(=商品登録済)
    this.registeredPartNoList = itemList.filter(item => item.registStatus === RegistStatus.PART); // 品番登録済リスト
  }

    /**
   * 品番一覧に追加取得分をリストに設定する。
   * @param itemList 品番情報リスト
   */
  private addItemList(itemList: Item[]): void {

    this.allItemList = this.allItemList.concat(itemList);   // 全品番リスト
    this.makerRegisteredList = this.makerRegisteredList.concat(
                                    itemList.filter(item => item.registStatus === RegistStatus.ITEM));  // メーカー登録済リスト(=商品登録済)
    this.registeredPartNoList = this.registeredPartNoList.concat(
                                    itemList.filter(item => item.registStatus === RegistStatus.PART)); // 品番登録済リスト
  }

  /**
   * 品番一覧リストのタブ変更時のイベント
   * @param event 選択タブの情報
   */
  onItemListTabChange(event: NgbTabChangeEvent): void {
    // 選択タブをsessionに保持
    this.localStorageService.createLocalStorage(this.session, this.STORAGE_KEY_ITEM_LIST_SELECTED_TAB_ID, event.nextId);
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
    this.itemService.getItemSearch({ pageToken: nextPageToken } as ItemSearchConditions).toPromise().then(
      item => {
        this.nextPageToken = item.nextPageToken; // 次のページのトークンを保存する
        const itemList = item['items'];
        if (itemList.length > 0) { // 検索結果あり
          // 検索結果1以上の場合はリストに設定する。
          this.addItemList(itemList);
        }
        this.loadingService.loadEnd();
      },
      error => {
        this.handleApiError(error);
      }
    );
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
