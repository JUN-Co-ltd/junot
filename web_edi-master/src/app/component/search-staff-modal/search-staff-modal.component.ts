import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ScrollEvent } from 'ngx-scroll-event';

import { AuthType, SearchTextType } from '../../const/const';
import { StringUtils } from '../../util/string-utils';

import { JunpcCodmstSearchCondition } from '../../model/junpc-codmst-search-condition';
import { JunpcCodmst } from '../../model/junpc-codmst';

import { JunpcCodmstService } from '../../service/junpc-codmst.service';
import { SessionService } from '../../service/session.service';
import { shallowEqualArrays } from '@angular/router/src/utils/collection';

class SearchResultItem {
  item: JunpcCodmst;
  selected: boolean;
}

@Component({
  selector: 'app-search-staff-modal',
  templateUrl: './search-staff-modal.component.html',
  styleUrls: ['./search-staff-modal.component.scss']
})
export class SearchStaffModalComponent implements OnInit {
  @Input() defaultStaffCode = ''; // 担当者コード
  @Input() defaultStaffName = ''; // 担当者名
  @Input() staffType = '';        // 職種
  @Input() brandCode = '';        // ブランドコード
  //PRD_0111 #7474 JFE add start
  @Input() defaultShowAllStaff = false; // ブランドによりリストは表示/非表示
  //PRD_0111 #7474 JFE add end

  /** 検索区分リスト */
  readonly searchTypes = [
    { id: SearchTextType.CODE_NAME_PARTIAL_MATCH, name: 'コード/名称', affiliation: AuthType.AUTH_INTERNAL },
    { id: SearchTextType.CODE_PARTIAL_MATCH, name: 'コード', affiliation: AuthType.AUTH_INTERNAL },
    { id: SearchTextType.NAME_PARTIAL_MATCH, name: '名称', affiliation: AuthType.AUTH_INTERNAL },
    { id: SearchTextType.NAME_PARTIAL_MATCH, name: '名称', affiliation: AuthType.AUTH_SUPPLIERS }
  ];

  // htmlから参照したい定数を定義
  readonly AuthType = AuthType;

  affiliation: AuthType;                      // ログインユーザの権限
  searchFormGroup: FormGroup;                 // 検索フォームグループ
  searchLoading: boolean;                     // 検索中フラグ（連続クリック防止用）
  searchResultItems: SearchResultItem[] = []; // 検索結果
  selectedItem: SearchResultItem;             // 選択中の行
  nextPageToken: string;                      // 次のページのトークン

  constructor(
    public activeModal: NgbActiveModal,
    public junpcCodmstService: JunpcCodmstService,
    public sessionService: SessionService
  ) { }

  ngOnInit() {
    this.affiliation = this.sessionService.getSaveSession().affiliation;
    // 社内の場合、'0'：コード/名称 部分一致検索、社外の場合、'2'：名称 部分一致検索
    const searchType = this.affiliation === AuthType.AUTH_INTERNAL ? '0' : '2';
    // 初期表示時は社内の場合はコードで検索、社外の場合は名称で検索
    const searchText = this.affiliation === AuthType.AUTH_INTERNAL ? this.defaultStaffCode : this.defaultStaffName;
     //PRD_0111 #7474 JFE add start
     const showAllStaff = false;
     //PRD_0111 #7474 JFE add end

    this.searchFormGroup = new FormGroup({
      searchType: new FormControl(searchType),
      searchText: new FormControl(searchText),
      //PRD_0111 #7474 JFE add start
      showAllStaff: new FormControl(showAllStaff)
      //PRD_0111 #7474 JFE add start
    });

    this.search(this.defaultStaffCode);
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

      this.junpcCodmstService.getStaffs({ pageToken: nextPageToken } as JunpcCodmstSearchCondition).subscribe(
        genericList => {
          this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
          this.searchResultItems = this.searchResultItems.concat(genericList.items.map(
            codmst => this.toSearchResultItem(codmst)));  // 検索結果を画面表示用に変換する
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
  private toSearchResultItem(item: JunpcCodmst): SearchResultItem {
    return { item: item, selected: false } as SearchResultItem;
  }

  /**
   * 検索条件をもとにデータを取得し、画面に結果を表示します。
   * @param defaultStaffCode 親ページで設定済の担当者コード
   */
  private search(defaultStaffCode = ''): void {
    this.searchLoading = true;
    this.searchResultItems = [];
    this.selectedItem = null;
    //PRD_0111 #7474 JFE add start
    let CodeBrand = this.brandCode;
    if (this.searchFormGroup.value.showAllStaff == true) {
      CodeBrand = '';
    }
    //PRD_0111 #7474 JFE add end

    this.junpcCodmstService.getStaffs({
      staffType: this.staffType,
      //PRD_0111 #7474 JFE mod start
      //brand: this.brandCode,
      brand: CodeBrand,
      //showAllStaff: this.searchFormGroup.value.showAllStaff,
    //PRD_0111 #7474 JFE mod end
      searchType: this.searchFormGroup.value.searchType,
      searchText: this.searchFormGroup.value.searchText
    } as JunpcCodmstSearchCondition).subscribe(
      genericList => {
        this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
        this.searchResultItems = genericList.items.map(codmst => this.toSearchResultItem(codmst));  // 検索結果を画面表示用に変換する

        if (defaultStaffCode !== '') { // 親ページで設定済の値があり再度モーダルを開いた時点である場合、選択状態にする
          this.searchResultItems.filter(result => result.item.code1 === defaultStaffCode)
            .forEach(resultItem => this.onSelectRow(resultItem));
        } else if (this.searchResultItems.length === 1) {
          this.onSelectRow(this.searchResultItems[0]);  // 1行のみの場合、先頭行を選択状態にする
        }
        this.searchLoading = false; // loading表示終了
      }
    );
  }
}
