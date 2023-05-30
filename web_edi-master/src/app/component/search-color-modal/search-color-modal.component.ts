import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ScrollEvent } from 'ngx-scroll-event';

import { StringUtils } from '../../util/string-utils';

import { JunpcCodmstSearchCondition } from '../../model/junpc-codmst-search-condition';
import { JunpcCodmst } from '../../model/junpc-codmst';

import { JunpcCodmstService } from '../../service/junpc-codmst.service';

class SearchResultItem {
  item: JunpcCodmst;
  selected: boolean;
}

@Component({
  selector: 'app-search-color-modal',
  templateUrl: './search-color-modal.component.html',
  styleUrls: ['./search-color-modal.component.scss']
})
export class SearchColorModalComponent implements OnInit {
  @Input() searchCondition: JunpcCodmstSearchCondition = null;  // 検索条件
  @Input() default: JunpcCodmst = null; // デフォルト値

  // 検索区分リスト
  readonly searchTypes = [
    { id: '0', name: 'コード/名称' },
    { id: '1', name: 'コード' },
    { id: '2', name: '名称' }
  ];

  searchFormGroup: FormGroup; // 検索フォームグループ
  searchLoading = false;      // 検索中フラグ（連続クリック防止用）
  searchResultItems: SearchResultItem[] = []; // 検索結果
  selectedItem: SearchResultItem; // 選択中の行
  nextPageToken = '';             // 次のページのトークン

  constructor(
    public activeModal: NgbActiveModal,
    public junpcCodmstService: JunpcCodmstService
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

      this.junpcCodmstService.getColors({ pageToken: nextPageToken } as JunpcCodmstSearchCondition).subscribe(
        genericList => {
          this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
          // 検索結果を画面表示用に変換する
          this.searchResultItems = this.searchResultItems.concat(genericList.items.map(codmst => this.toSearchResultItem(codmst)));
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
   * @param selectedItem 選択された行のデータ
   */
  onSelectRow(selectedItem: SearchResultItem): void {
    this.searchResultItems.filter(item => item.selected).forEach(item => item.selected = false);  // 他の行の選択を解除
    selectedItem.selected = true;     // 行を選択状態にする
    this.selectedItem = selectedItem; // 選択行のデータを保持する
  }

  /**
   * 画面表示用に検索結果を変換します。
   * @param item 検索結果
   * @return 検索結果に表示する結果
   */
  private toSearchResultItem(item: JunpcCodmst): SearchResultItem {
    return { item: item, selected: false } as SearchResultItem;
  }

  /**
   * 検索条件をもとにデータを取得し、画面に結果を表示します。
   * @param defaultItem デフォルト値
   */
  private search(defaultItem: JunpcCodmst): void {
    this.searchLoading = true;
    this.searchResultItems = [];
    this.selectedItem = null;

    this.junpcCodmstService.getColors({
      searchType: this.searchFormGroup.value.searchType,
      searchText: this.searchFormGroup.value.searchText
    } as JunpcCodmstSearchCondition).subscribe(
      genericList => {
        this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
        // 検索結果を画面表示用に変換する
        this.searchResultItems = genericList.items.map(codmst => this.toSearchResultItem(codmst));

        if (defaultItem != null && StringUtils.isNotEmpty(defaultItem.code1)) { // デフォルト値がある場合、対象行を選択状態にする
          this.searchResultItems.filter(result => result.item.code1 === defaultItem.code1)
            .forEach(resultItem => this.onSelectRow(resultItem));
        } else if (this.searchResultItems.length === 1) {
          this.onSelectRow(this.searchResultItems[0]);  // 1行のみの場合、先頭行を選択状態にする
        }
        this.searchLoading = false;
      });
  }
}
