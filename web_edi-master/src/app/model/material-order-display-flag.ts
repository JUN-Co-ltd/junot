/**
 * 資材発注(フクキタル)項目表示フラグのModel.
 */
export class MaterialOrderDisplayFlag {
  /** カテゴリコード表示フラグ */
  isDisplayCategoryCode: boolean;

  /** サスティナブルマーク印字表示フラグ */
  isDisplaySustainableMark: boolean;

  /** 中国内販内販：製品分類 表示フラグ */
  isDisplayCnProductCategory: boolean;

  /** 中国内販：製品種別 表示フラグ */
  isDisplayCnProductType: boolean;

  constructor(isDisplay: boolean) {
    this.isDisplayCategoryCode = isDisplay;
    this.isDisplaySustainableMark = isDisplay;
    this.isDisplayCnProductCategory = isDisplay;
    this.isDisplayCnProductType = isDisplay;
  }
}
