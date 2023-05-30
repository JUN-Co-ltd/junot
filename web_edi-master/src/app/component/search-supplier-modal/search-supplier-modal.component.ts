import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ScrollEvent } from 'ngx-scroll-event';

import { StringUtils } from '../../util/string-utils';

import { JunpcSirmstSearchCondition } from '../../model/junpc-sirmst-search-condition';
import { JunpcSirmst } from '../../model/junpc-sirmst';

import { JunpcSirmstService } from '../../service/junpc-sirmst.service';

class SearchResultItem {
  item: JunpcSirmst;
  selected: boolean;
}

@Component({
  selector: 'app-search-supplier-modal',
  templateUrl: './search-supplier-modal.component.html',
  styleUrls: ['./search-supplier-modal.component.scss']
})
export class SearchSupplierModalComponent implements OnInit {
  @Input() searchCondition: JunpcSirmstSearchCondition = null;  // 検索条件
  @Input() default: JunpcSirmst = null; // デフォルト値

  /** 検索区分リスト */
  readonly searchTypes = [
    { id: '0', name: 'コード/名称' },
    { id: '1', name: 'コード' },
    { id: '2', name: '名称' }
  ];

  searchFormGroup: FormGroup;  // 検索フォームグループ
  searchLoading = false;  // 検索中フラグ（連続クリック防止用）
  searchResultItems: SearchResultItem[] = []; // 検索結果
  selectedItem: SearchResultItem;  // 選択中の行
  nextPageToken = '';  // 次のページのトークン

  constructor(
    public activeModal: NgbActiveModal,
    public junpcSirmstService: JunpcSirmstService
  ) { }

  ngOnInit() {
    this.searchFormGroup = new FormGroup({
      searchType: new FormControl(this.searchCondition.searchType),
      searchText: new FormControl(this.searchCondition.searchText)
    });

    this.search(this.default);
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

      this.junpcSirmstService.getSirmst({ pageToken: nextPageToken } as JunpcSirmstSearchCondition).subscribe(
        genericList => {
          // 次のページのトークンを保存する
          this.nextPageToken = genericList.nextPageToken;
          // 検索結果を画面表示用に変換する
          this.searchResultItems = this.searchResultItems.concat(genericList.items.map(sirmst => this.toSearchResultItem(sirmst)));
        });
    }
  }

  /**
   * 検索ボタンクリック時、検索条件をもとにデータを取得し、画面に結果を表示します。
   */
  onSearch(): void {
    this.search(null);
  }

  /**
   * 選択ボタンクリック時、親ページに選択行のデータを渡して、モーダルを閉じます。
   */
  onSelect(): void {
    this.activeModal.close(this.selectedItem.item);
  }

  /**
   * 行選択時、選択行のデータを保持し、選択行にのみselectedクラスを付与します。
   *
   * @param selectedItem 選択された行のデータ
   */
  onSelectRow(selectedItem: SearchResultItem): void {
    this.searchResultItems.filter(item => item.selected).forEach(item => item.selected = false);  // 他の行の選択を解除
    selectedItem.selected = true;     // 行を選択状態にする
    this.selectedItem = selectedItem; // 選択行のデータを保持する
  }

  /**
   * 画面表示用に検索結果を変換します。
   *
   * @param item 検索結果
   * @return 検索結果に表示する結果
   */
  private toSearchResultItem(item: JunpcSirmst): SearchResultItem {
    return { item: item, selected: false } as SearchResultItem;
  }

  /**
   * 検索条件をもとにデータを取得し、画面に結果を表示します。
   * @param defaultItem デフォルト値
   */
  private search(defaultItem: JunpcSirmst): void {
    this.searchLoading = true;
    this.searchResultItems = [];
    this.selectedItem = null;

    this.junpcSirmstService.getSirmst({
      sirkbn: this.searchCondition.sirkbn,
      searchType: this.searchFormGroup.value.searchType,
      searchText: this.searchFormGroup.value.searchText
    } as JunpcSirmstSearchCondition).subscribe(
      genericList => {
        this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
        this.searchResultItems = genericList.items.map(sirmst => this.toSearchResultItem(sirmst));  // 検索結果を画面表示用に変換する

        if (defaultItem != null && StringUtils.isNotEmpty(defaultItem.sire)) {
          // デフォルト値がある場合、対象行を選択状態にする
          this.searchResultItems.filter(result => result.item.sire === defaultItem.sire)
            .forEach(resultItem => this.onSelectRow(resultItem));
        } else if (this.searchResultItems.length === 1) {
          this.onSelectRow(this.searchResultItems[0]);  // 1行のみの場合、先頭行を選択状態にする
        }

        this.searchLoading = false;
      }
    );
  }
}
