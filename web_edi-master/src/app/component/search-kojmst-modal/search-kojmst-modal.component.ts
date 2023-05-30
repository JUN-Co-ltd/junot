import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ScrollEvent } from 'ngx-scroll-event';

import { AuthType } from '../../const/const';
import { StringUtils } from '../../util/string-utils';

import { JunpcKojmstSearchCondition } from '../../model/junpc-kojmst-search-condition';
import { JunpcKojmst } from '../../model/junpc-kojmst';

import { JunpcKojmstService } from '../../service/junpc-kojmst.service';
import { SessionService } from '../../service/session.service';

class SearchResultItem {
  item: JunpcKojmst;
  selected: boolean;
}

@Component({
  selector: 'app-search-kojmst-modal',
  templateUrl: './search-kojmst-modal.component.html',
  styleUrls: ['./search-kojmst-modal.component.scss']
})
export class SearchKojmstModalComponent implements OnInit {
  @Input() searchCondition: JunpcKojmstSearchCondition = null;  // 検索条件
  @Input() default: JunpcKojmst = null; // デフォルト値

  /** 検索区分リスト */
  readonly searchTypes = [
    { id: '0', name: 'コード/名称', affiliation: AuthType.AUTH_INTERNAL },
    { id: '1', name: 'コード', affiliation: AuthType.AUTH_INTERNAL },
    { id: '2', name: '名称', affiliation: AuthType.AUTH_INTERNAL },
    { id: '0', name: 'コード/名称', affiliation: AuthType.AUTH_SUPPLIERS },
    { id: '1', name: 'コード', affiliation: AuthType.AUTH_SUPPLIERS },
    { id: '2', name: '名称', affiliation: AuthType.AUTH_SUPPLIERS }
  ];

  /** htmlから参照したい定数を定義 */
  readonly AuthType = AuthType;

  affiliation: AuthType;                      // ユーザー権限
  searchFormGroup: FormGroup;                 // 検索フォームグループ
  searchLoading: boolean;                     // 検索中フラグ（連続クリック防止用）
  searchResultItems: SearchResultItem[] = []; // 検索結果
  selectedItem: SearchResultItem;             // 選択中の行
  nextPageToken: string;                      // 次のページのトークン

  constructor(
    public activeModal: NgbActiveModal,
    public junpcKojmstService: JunpcKojmstService,
    public sessionService: SessionService
  ) { }

  ngOnInit() {
    this.affiliation = this.sessionService.getSaveSession().affiliation;

    this.searchFormGroup = new FormGroup({
      searchType: new FormControl(this.searchCondition.searchType),
      searchText: new FormControl(this.searchCondition.searchText),
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

      this.junpcKojmstService.getKojmst({ pageToken: nextPageToken } as JunpcKojmstSearchCondition).subscribe(
        genericList => {
          this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
          // 検索結果を画面表示用に変換する
          this.searchResultItems = this.searchResultItems.concat(genericList.items.map(kojmst => this.toSearchResultItem(kojmst)));
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
  private toSearchResultItem(item: JunpcKojmst): SearchResultItem {
    return { item: item, selected: false } as SearchResultItem;
  }

  /**
   * 検索条件をもとにデータを取得し、画面に結果を表示します。
   */
  private search(defaultItem: JunpcKojmst): void {
    this.searchLoading = true;
    this.searchResultItems = [];
    this.selectedItem = null;

    this.junpcKojmstService.getKojmst({
      sire: this.searchCondition.sire,
      sirkbn: this.searchCondition.sirkbn,
      brand: this.searchCondition.brand,
      searchType: this.searchFormGroup.value.searchType,
      searchText: this.searchFormGroup.value.searchText
    } as JunpcKojmstSearchCondition).subscribe(
      genericList => {
        this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
        this.searchResultItems = genericList.items.map(kojmst => this.toSearchResultItem(kojmst));  // 検索結果を画面表示用に変換する

        if (defaultItem != null && StringUtils.isNotEmpty(defaultItem.kojcd)) { // デフォルト値がある場合、対象行を選択状態にする
          this.searchResultItems.filter(result => result.item.kojcd === defaultItem.kojcd)
            .forEach(resultItem => this.onSelectRow(resultItem));
        } else if (this.searchResultItems.length === 1) {
          this.onSelectRow(this.searchResultItems[0]);  // 1行のみの場合、先頭行を選択状態にする
        }
        this.searchLoading = false;
      }
    );
  }
}
