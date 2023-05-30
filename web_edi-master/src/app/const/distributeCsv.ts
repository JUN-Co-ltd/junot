/**
 * 店舗配分CSV項目名.
 */
export class DistributeCsvColumn {
  /** 発注番号 */
  public static readonly ORDER_NUMBER = '発注番号';
  /** 回数 */
  public static readonly DELIVERY_COUNT = '回数';
  /** 納品依頼番号 */
  public static readonly DELIVERY_NUMBER = '納品依頼番号';
  /** 品番 */
  public static readonly PART_NO = '品番';
  /** 商品名 */
  public static readonly PRODUCT_NAME = '商品名';
  /** タイトル */
  public static readonly TITLE = 'タイトル';
  /** No */
  public static readonly NO = 'No';
  /** 店舗コード */
  public static readonly SHOP_CODE = '店舗コード';
  /** 店舗名 */
  public static readonly SHOP_NAME = '店舗名';
  /** 配分率 */
  public static readonly DISTRIBUTE_RATIO = '配分率';
  /** 配分数 */
  public static readonly DISTRIBUTE = '配分数';
  /** 在庫数 */
  public static readonly STOCK = '在庫数';
  /** 売上数 */
  public static readonly SALES = '売上数';
  // PRD_0119#8396 add JFE start
  /** SKU */
  public static readonly SKU = 'SKU';
  // PRD_0119#8396 add JFE end
  /** カラー */
  public static readonly COLOR = 'カラー';
  /** サイズ */
  public static readonly SIZE = 'サイズ';
  /** 合計数 */
  public static readonly TOTAL = '合計数';
  /** 課合計 */
  public static readonly DIVISION_TOTAL = '課合計';
  /** 明細 */
  public static readonly DETAIL = '明細';
}
