export class TabModel {
  /** タブ名 */
  name: string;
  /** コンテンツ */
  contents: any;
  /** 現在表示中か否かを示すフラグ */
  current: boolean;

  /**
   * コンストラクタ
   *
   * @param {string} name タブ名
   * @param {*} contents コンテンツ( 実態はコンポーネントそのもの )
   * @param {boolean} current 現在表示中か否かを示すフラグ
   */
  constructor(name: string, contents: any, current: boolean) {
    this.name = name;
    this.contents = contents;
    this.current = current;
  }
}
