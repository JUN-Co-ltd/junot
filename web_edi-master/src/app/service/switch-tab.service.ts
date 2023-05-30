import { Injectable } from '@angular/core';
import { TabModel } from '../model/tab-model';

@Injectable({
  providedIn: 'root'
})
export class SwitchTabService {
  private tabs: TabModel[] = new Array<TabModel>();

  /** コンストラクタ */
  constructor() { }

  /**
   * タブのListを設定する。
   * @param tabContentList タブの内容リスト
   */
  public setTabContentList(tabContentList: TabModel[]): void {
    this.tabs = tabContentList.concat();
  }

  /**
   * 現在表示中のコンテンツを取得する
   * @returns 現在表示中のコンテンツ
   */
  public getCurrentContents(): any {
    for (const target in this.tabs) {
      if (this.tabs.hasOwnProperty(target) && this.tabs[target].current) {
        return this.tabs[target].contents;
      }
    }
  }

  /**
   * 表示するコンテンツを切り替える
   * @param name クリックされたタブのタブ名
   * @returns 切り替え先のコンテンツ
   */
  public changeCurrentContents(name: string): any {
    let contents: any;
    for (const target in this.tabs) {
      if (this.tabs.hasOwnProperty(target)) {
        this.tabs[target].current = false;

        if (this.tabs[target].name === name) {
          this.tabs[target].current = true;
          contents = this.tabs[target].contents;
        }
      }
    }
    return contents;
  }
}
