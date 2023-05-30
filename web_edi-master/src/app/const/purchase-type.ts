
/**
 * 仕入区分.
 */
export class PurchaseType {
  /** 追加仕入 */
  public static readonly ADDITIONAL_PURCHASE = 1;
  /** 追加仕入赤 */
  public static readonly ADDITIONAL_PURCHASE_RED = 2;
  /** 仕入返品 */
  public static readonly RETURN_PURCHASE = 3;
  /** 仕入返品赤 */
  public static readonly RETURN_PURCHASE_RED = 4;
  /** 配分出荷仕入(一括) */
  public static readonly SHIPMENT_PURCHASE_LUMP = 5;
  /** 配分出荷仕入(課別) */
  public static readonly SHIPMENT_PURCHASE_DIVISION = 6;
  /** 直送仕入 */
  public static readonly DIRECT_PURCHASE = 7;
  /** 店舗発注分仕入 */
  public static readonly STORE_PURCAHSE = 9;
}
