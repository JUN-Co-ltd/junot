import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ScrollEvent } from 'ngx-scroll-event';

import { FukukitaruMasterType } from '../../const/const';
import { StringUtils } from '../../util/string-utils';

import { ScreenSettingFukukitaruOrderSearchCondition } from '../../model/screen-setting-fukukitaru-order-search-condition';
import { FukukitaruDestination } from '../../model/fukukitaru-destination';

import { FukukitaruOrder01Service } from '../../service/fukukitaru-order01.service';

class SearchResultItem {
  item: FukukitaruDestination;
  selected: boolean;
}

@Component({
  selector: 'app-search-company-modal',
  templateUrl: './search-company-modal.component.html',
  styleUrls: ['./search-company-modal.component.scss']
})
export class SearchCompanyModalComponent implements OnInit {
  @Input() defaultCompanyId = null;   // 宛先情報の会社ID
  @Input() listMasterType = null;     // マスタタイプリスト
  @Input() partNoId = null;           // 品番ID
  @Input() orderId = null;            // 発注ID
  @Input() deliveryType = null;       // フクキタルデリバリ種別
  @Input() searchCompanyName = '';    // 検索会社名

  searchFormGroup: FormGroup;                 // 検索フォームグループ
  searchLoading: boolean;                     // 検索中フラグ（連続クリック防止用）
  searchResultItems: SearchResultItem[] = []; // 検索結果
  selectedItem: SearchResultItem;             // 選択中の行
  nextPageToken: string;                      // 次のページのトークン

  billingCompany: boolean;   // タイトル(請求先)
  deliveryCompany: boolean;  // タイトル(納入先)

  searchListMasterType: FukukitaruMasterType[] = [];

  constructor(
    public activeModal: NgbActiveModal,
    public fukukitaruOrder01Service: FukukitaruOrder01Service
  ) { }

  ngOnInit() {
    // 検索用マスタタイプリストを設定
    if (this.listMasterType === FukukitaruMasterType.BILLING_ADDRESS) {
      this.searchListMasterType[0] = FukukitaruMasterType.BILLING_ADDRESS; // 請求先会社名
    } else if (this.listMasterType === FukukitaruMasterType.DERIVERY_ADDRESS) {
      this.searchListMasterType[0] = FukukitaruMasterType.DERIVERY_ADDRESS; // 納入先会社名
    }

    // 検索パラメタをフォームに設定
    this.searchFormGroup = new FormGroup({
      listMasterType: new FormControl(this.searchListMasterType),
      partNoId: new FormControl(this.partNoId),
      orderId: new FormControl(this.orderId),
      deliveryType: new FormControl(this.deliveryType),
      searchCompanyName: new FormControl(this.searchCompanyName),
    });

    this.search(this.defaultCompanyId); // 検索

    // タイトル
    switch (this.listMasterType) {
      case FukukitaruMasterType.BILLING_ADDRESS: // 請求先会社名
        this.billingCompany = true;
        break;
      case FukukitaruMasterType.DERIVERY_ADDRESS: // 納入先会社名
        this.deliveryCompany = true;
        break;
      default:
        break;
    }
  }

  /**
   * スクロール時、最下部までスクロールされた場合は、次の検索結果を取得します。
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    if (event.isReachingBottom && StringUtils.isNotEmpty(this.nextPageToken)) {
      // 一番下までスクロールされ、次のページのトークンがある場合、次のリストを取得する
      const nextPageToken = this.nextPageToken;
      this.nextPageToken = null;
      // 請求先会社名を検索
      this.fukukitaruOrder01Service.getAddress({
        pageToken: nextPageToken,
        listMasterType: this.searchFormGroup.value.listMasterType,       // マスタタイプリスト
        partNoId: this.searchFormGroup.value.partNoId,                   // 品番ID
        orderId: this.searchFormGroup.value.orderId,                     // 発注ID
        deliveryType: this.searchFormGroup.value.deliveryType,           // デリバリ種別
        searchCompanyName: this.searchFormGroup.value.searchCompanyName  // 検索会社名
      } as ScreenSettingFukukitaruOrderSearchCondition).subscribe(
        genericList => {
          this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
          if (this.listMasterType === FukukitaruMasterType.BILLING_ADDRESS) {
            // 請求先
            this.searchResultItems = this.searchResultItems.concat(genericList.items[0].listBillingAddress.map(
              fAddress => this.toSearchResultItem(fAddress)));  // 検索結果を画面表示用に変換する
          } else if (this.listMasterType === FukukitaruMasterType.DERIVERY_ADDRESS) {
            // 納入先
            this.searchResultItems = this.searchResultItems.concat(genericList.items[0].listDeriveryAddress.map(
              fAddress => this.toSearchResultItem(fAddress)));  // 検索結果を画面表示用に変換する
          }
        });
    }
  }

  /**
  * 検索ボタンクリック時、検索条件をもとにデータを取得し、画面に結果を表示します。
  */
  onSearch(): void {
    this.search();
  }

  /**
   * 選択ボタンクリック時、親ページに選択行のデータを渡して、モーダルを閉じます。
   */
  onSelect(): void {
    this.activeModal.close(this.selectedItem.item);
  }

  /**
   * 行選択時、選択行のデータを保持し、選択行にのみselectedクラスを付与します。
   * @param selectedItem 選択された行のデータ
   */
  onSelectRow(selectedItem: SearchResultItem): void {
    this.searchResultItems.filter(x => x.selected).forEach(x => x.selected = false);  // 他の行の選択を解除
    selectedItem.selected = true;     // 行を選択状態にする
    this.selectedItem = selectedItem; // 選択行のデータを保持する
  }

  /**
   * 画面表示用に検索結果を変換します。
   * @param item 検索結果
   * @return 検索結果に表示する結果
   */
  private toSearchResultItem(item: FukukitaruDestination): SearchResultItem {
    return { item: item, selected: false } as SearchResultItem;
  }

  /**
   * 検索条件をもとにデータを取得し、画面に結果を表示します。
   * @param defaultCompanyId 親ページで設定済の会社id
   */
  private search(defaultCompanyId = null): void {
    this.searchLoading = true;
    this.searchResultItems = [];
    this.selectedItem = null;

    this.fukukitaruOrder01Service.getAddress({
      listMasterType: this.searchFormGroup.value.listMasterType,       // マスタタイプリスト
      partNoId: this.searchFormGroup.value.partNoId,                   // 品番ID
      orderId: this.searchFormGroup.value.orderId,                     // 発注ID
      deliveryType: this.searchFormGroup.value.deliveryType,           // デリバリ種別
      searchCompanyName: this.searchFormGroup.value.searchCompanyName  // 検索会社名
    } as ScreenSettingFukukitaruOrderSearchCondition).subscribe(
      genericList => {
        this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する

        // 請求先会社名の検索結果を画面表示用に変換する
        if (this.listMasterType === FukukitaruMasterType.BILLING_ADDRESS) {
          this.searchResultItems = genericList.items[0].listBillingAddress.map(fAddress => this.toSearchResultItem(fAddress));
        }
        // 納入先会社名の検索結果を画面表示用に変換する
        if (this.listMasterType === FukukitaruMasterType.DERIVERY_ADDRESS) {
          this.searchResultItems = genericList.items[0].listDeriveryAddress.map(fAddress => this.toSearchResultItem(fAddress));
        }

        if (defaultCompanyId !== null) { // 親ページで設定済の値があり再度モーダルを開いた時点である場合、選択状態にする
          this.searchResultItems.filter(result => result.item.id === defaultCompanyId)
            .forEach(resultItem => this.onSelectRow(resultItem));
        } else if (this.searchResultItems.length === 1) {
          this.onSelectRow(this.searchResultItems[0]);  // 1行のみの場合、先頭行を選択状態にする
        }
        this.searchLoading = false; // loading表示終了
      }
    );
  }
}
