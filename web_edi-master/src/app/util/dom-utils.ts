/**
 * DOMユーティリティ.
 */
export class DOMUtils {
  constructor(
  ) { }

  /** TYPE_FORM_GROUP:0 */
  public static readonly TYPE_FORM_GROUP: number = 0;
  /** TYPE_CONTROL:1 */
  public static readonly TYPE_CONTROL: number = 1;
  /** ACTION_ADD:1 */
  public static readonly ACTION_ADD: number = 1;
  /** ACTION_REMOVE:0 */
  public static readonly ACTION_REMOVE: number = 0;

  /**
   * 必須切り替え.
   * 対象エレメントのname、エレメントの種類、追加/削除区分を受け取り、
   * 対象エレメントの必須切り替えを行います。
   *
   * @param name 対象エレメントのname属性
   * @param type エレメントの種類(TYPE_FORM_GROUP/TYPE_CONTROL)
   * @param action 追加/削除区分(ACTION_ADD/ACTION_REMOVE)
   */
  static changeRequiredView(name: string, type: number, action: number): void {
    // エレメント取得
    const elements: NodeListOf<HTMLElement> = document.getElementsByName(name);

    for (let i = 0; i < elements.length; i++) {
      if (action !== this.ACTION_ADD) {
        // 必須対象外の場合
        if (type === this.TYPE_FORM_GROUP) {
          elements[i].classList.remove('form-group');
          elements[i].classList.remove('required');
        } else if (type === this.TYPE_CONTROL) {
          elements[i].removeAttribute('required');
          elements[i].setAttribute('ng-reflect-required', 'false');
        }
      } else {
        // 必須対象の場合
        if (type === this.TYPE_FORM_GROUP) {
          elements[i].classList.add('form-group');
          elements[i].classList.add('required');
        } else if (type === this.TYPE_CONTROL) {
          elements[i].setAttribute('required', '');
          elements[i].setAttribute('ng-reflect-required', 'true');
        }
      }
    }
  }
}
