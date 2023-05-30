import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Location } from '@angular/common';

import { SessionStorageKey, SelectTabName, SelectTabUrl } from '../../const/const';

import { TabModel } from '../../model/tab-model';

import { HeaderService } from '../../service/header.service';
import { ClientSessionService } from '../../service/client-session.service';
import { SwitchTabService } from '../../service/switch-tab.service';

import { ItemListComponent } from '../../component/item-list/item-list.component';
import { OrderListComponent } from '../../component/order-list/order-list.component';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {
  currentTab: any;
  tabs: TabModel[] = new Array<TabModel>();

  /** コンストラクタ */
  constructor(
    private router: Router,
    private location: Location,
    private switchTabService: SwitchTabService,
    private headerService: HeaderService,
    private clientSessionService: ClientSessionService
  ) { }

  /** 初期処理 */
  ngOnInit() {
    this.headerService.show();
    this.setInitTab();
  }

  /** タブの初期表示処理を行う。 */
  private setInitTab(): void {
    // sessionから前回選択したタブ名を取得
    const selectedTabName = this.getSaveSelectedTabName();

    // タブを設定する。
    this.tabs.push(new TabModel(SelectTabName.ITEM_LIST, ItemListComponent, selectedTabName === SelectTabName.ITEM_LIST));
    this.tabs.push(new TabModel(SelectTabName.ORDER_LIST, OrderListComponent, selectedTabName === SelectTabName.ORDER_LIST));
    this.switchTabService.setTabContentList(this.tabs);

    // クリックされたタブに応じてURLを切り替える
    this.location.replaceState(this.convertTabNameToUrl(selectedTabName));

    // 選択中のタブを表示する。
    this.currentTab = this.switchTabService.getCurrentContents();
  }

  /**
   * タブがクリックされた時のイベントハンドラ
   * @param tabName 選択したタブ名
   */
  onTabClick(tabName: string): void {
    // クリックされたタブに応じてURLを切り替える
    this.location.replaceState(this.convertTabNameToUrl(tabName));
    // クリックされたタブに応じて表示するコンテンツを切り替える
    this.currentTab = this.switchTabService.changeCurrentContents(tabName);
    // 選択タブをsessionに保持
    this.clientSessionService.createSession(SessionStorageKey.LIST_SELECTED_TAB_NAME, tabName);
  }

  /**
   * sessionから前回選択したタブ名を取得する
   */
  private getSaveSelectedTabName(): string {
    // sessionから前回選択したタブ名を取得
    let selectedTabName = JSON.parse(this.clientSessionService.getSaveSession(SessionStorageKey.LIST_SELECTED_TAB_NAME));

    if (selectedTabName == null) {
      selectedTabName = this.convertUrlToTabName(this.router.url);
    }

    return selectedTabName;
  }

  /**
   * URLをタブ名に変換する
   * @param url URL
   */
  private convertUrlToTabName(url: string): string {
    if (url.startsWith(SelectTabUrl.ITEM_LIST)) {
      return SelectTabName.ITEM_LIST;
    } else if (url.startsWith(SelectTabUrl.ORDER_LIST)) {
      return SelectTabName.ORDER_LIST;
    }

    return SelectTabName.ITEM_LIST;
  }

  /**
   * タブ名をURLに変換する
   * @param tabName 選択したタブ名
   */
  private convertTabNameToUrl(tabName: string): string {
    if (tabName === SelectTabName.ITEM_LIST) {
      return SelectTabUrl.ITEM_LIST;
    } else if (tabName === SelectTabName.ORDER_LIST) {
      return SelectTabUrl.ORDER_LIST;
    }

    return SelectTabUrl.ITEM_LIST;
  }
}
